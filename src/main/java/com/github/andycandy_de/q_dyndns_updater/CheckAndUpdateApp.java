package com.github.andycandy_de.q_dyndns_updater;

import com.github.andycandy_de.q_dyndns_updater.config.Config;
import com.github.andycandy_de.q_dyndns_updater.config.DynDnsConfig;
import com.github.andycandy_de.q_dyndns_updater.helper.Helper;
import com.github.andycandy_de.q_dyndns_updater.http_client.IDynDnsUpdate;
import com.github.andycandy_de.q_dyndns_updater.self_checker.SelfChecker;
import com.github.andycandy_de.q_dyndns_updater.wanted_ip_resolver.WantedIpResolver;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.Startup;
import io.smallrye.context.api.ManagedExecutorConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.jboss.logging.Logger;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class CheckAndUpdateApp {

    AtomicInteger times = new AtomicInteger(0);

    @Inject
    Logger logger;

    @Inject
    Helper helper;

    @Inject
    Config config;

    @Inject
    SelfChecker selfChecker;

    @Inject
    WantedIpResolver wantedIpResolver;

    @Inject
    @ManagedExecutorConfig(maxAsync = 1, propagated = ThreadContext.CDI)
    ManagedExecutor managedExecutor;

    @Startup
    public void startUp() {
        managedExecutor.execute(this::checkAndUpdate);
    }

    public void checkAndUpdate() {
        try {
            if (Boolean.TRUE.equals(config.getLogMemoryInfo())) {
                helper.logMemoryInfo();
            }

            final String checkDomainIp = Optional.of(config)
                    .filter(c -> Boolean.TRUE.equals(c.getSelfCheck()))
                    .map(Config::getSelfCheckUrl)
                    .map(helper::getHost)
                    .map(helper::findDnsIp)
                    .orElse(null);

            if (checkDomainIp != null && selfChecker.selfCheck()) {
                List<DynDnsConfig> toUpdateList = findInvalidDomains(config.getDynDnsConfigs(), checkDomainIp);
                if (toUpdateList.isEmpty()) {
                    logger.info("All domains have the correct ip! No update needed!");
                } else {
                    logger.info("Domains with wrong ip found!");
                    updateDynDns(toUpdateList, checkDomainIp);
                }
            } else {
                logger.info("Try to fetch wanted IP!");
                final String wantedIp = wantedIpResolver.resolveWantedIp();
                if (wantedIp == null) {
                    logger.warn("Unable to fetch external IP! Unable to update!");
                } else {
                    logger.info("Wanted ip is '%s' and self check domain ip is %s!".formatted(wantedIp, checkDomainIp));
                    List<DynDnsConfig> toUpdateList = findInvalidDomains(config.getDynDnsConfigs(), wantedIp);
                    if (toUpdateList.isEmpty()) {
                        logger.info("All domains have the correct ip! No update needed!");
                    } else {
                        logger.info("Domains with wrong ip found!");
                        updateDynDns(toUpdateList, wantedIp);
                    }
                }
            }
        } finally {
            final int currentTimes = times.incrementAndGet();
            Optional.of(config)
                    .map(Config::getTimes)
                    .filter(i -> i > 0)
                    .filter(i -> currentTimes >= i)
                    .ifPresentOrElse(
                            ignore -> Quarkus.asyncExit(),
                            () -> managedExecutor.execute(this::sleepAndRepeat)
                    );
        }
    }

    public void sleepAndRepeat() {
        Optional.of(config)
                .map(Config::getInterval)
                .map(Duration::toMillis)
                .ifPresent(this::sleep);
        managedExecutor.execute(this::checkAndUpdate);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private void updateDynDns(List<DynDnsConfig> toUpdateList, String wantedIp) {

        logger.info("Try to update DynDNS with IP:%s".formatted(wantedIp));

        for (DynDnsConfig dynDnsConfig : toUpdateList) {
            updateDynDns(dynDnsConfig, wantedIp);
        }
    }

    private void updateDynDns(DynDnsConfig dynDnsConfig, String wantedIp) {

        if (Boolean.TRUE.equals(dynDnsConfig.getDomainsSingleUpdate())) {
            for (String domain : dynDnsConfig.getDomains()) {
                updateDynDns(dynDnsConfig, List.of(domain), wantedIp);
            }
        } else {
            updateDynDns(dynDnsConfig, dynDnsConfig.getDomains(), wantedIp);
        }
    }

    private void updateDynDns(DynDnsConfig dynDnsConfig, List<String> domains, String wantedIp) {

        final String username = dynDnsConfig.getUsername();
        final String password = dynDnsConfig.getPassword();

        final URI uri = helper.createUri(dynDnsConfig.getUpdateUrl(), username, password);

        final String authHeader = helper.getAuthHeader(username, password);
        final String userAgent = dynDnsConfig.getUserAgent();

        try {
            final String response = helper.createRestClient(uri, IDynDnsUpdate.class)
                    .update(authHeader, userAgent, domains, wantedIp);

            if (response.startsWith("good")) {
                logger.info("Update successful with response '%s' for domains %s!".formatted(response, domains));
            } else if (response.startsWith("nochg")) {
                logger.info("Update successful with no changes with response '%s' for domains %s!".formatted(response, domains));
            } else {
                logger.warn("Update not successful with response '%s' for domains '%s'!".formatted(response, domains));
            }
        } catch (Exception e) {
            logger.error("Unable to update IP for domains '%s'".formatted(domains), e);
        }
    }

    private List<DynDnsConfig> findInvalidDomains(List<DynDnsConfig> dynDnsConfigs, String wantedIp) {

        return dynDnsConfigs.stream()
                .map(e -> {
                    final List<String> domains = e.getDomains().stream()
                            .filter(d ->  !Objects.equals(wantedIp, helper.findDnsIp(d)))
                            .toList();

                    if (domains.isEmpty()) {
                        return null;
                    } else {
                        return e.toBuilder()
                                .domains(domains)
                                .build();
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
