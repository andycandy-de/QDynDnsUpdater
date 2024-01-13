package com.github.andycandy_de.q_dyndns_updater;

import com.github.andycandy_de.q_dyndns_updater.config.DynDnsConfig;
import com.github.andycandy_de.q_dyndns_updater.http_client.IDynDnsUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;

@ApplicationScoped
public class DomainUpdater {

    @Inject
    Logger logger;

    @Inject
    Helper helper;

    public void updateDynDns(List<DynDnsConfig> toUpdateList, String ip) {

        logger.info("Try to update DynDNS with IP:%s".formatted(ip));

        for (DynDnsConfig dynDnsConfig : toUpdateList) {
            updateDynDns(dynDnsConfig, ip);
        }
    }

    private void updateDynDns(DynDnsConfig dynDnsConfig, String ip) {

        if (Boolean.TRUE.equals(dynDnsConfig.getDomainsSingleUpdate())) {
            for (String domain : dynDnsConfig.getDomains()) {
                updateDynDns(dynDnsConfig, List.of(domain), ip);
            }
        } else {
            updateDynDns(dynDnsConfig, dynDnsConfig.getDomains(), ip);
        }
    }

    private void updateDynDns(DynDnsConfig dynDnsConfig, List<String> domains, String ip) {

        final String username = dynDnsConfig.getUsername();
        final String password = dynDnsConfig.getPassword();

        final URI uri = helper.createUri(dynDnsConfig.getUpdateUrl(), username, password);

        final String authHeader = helper.getAuthHeader(username, password);
        final String userAgent = dynDnsConfig.getUserAgent();

        try {
            final String response = helper.createRestClient(uri, IDynDnsUpdate.class)
                    .update(authHeader, userAgent, domains, ip);

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
}
