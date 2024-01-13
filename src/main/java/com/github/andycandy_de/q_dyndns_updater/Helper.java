package com.github.andycandy_de.q_dyndns_updater;

import com.github.andycandy_de.q_dyndns_updater.config.DynDnsConfig;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class Helper {

    @Inject
    Logger logger;

    public <T> T createRestClient(URI uri, Class<T> clazz) {
        return QuarkusRestClientBuilder
                .newBuilder()
                .baseUri(uri)
                .build(clazz);
    }

    public URI createUri(URI updateUrl, String username, String password) {
        final String userInfo = "%s:%s".formatted(username, password);
        return UriBuilder.fromUri(updateUrl).userInfo(userInfo).build();
    }

    public String getAuthHeader(String username, String password) {
        return Optional.of("%s:%s".formatted(username, password))
                .map(String::getBytes)
                .map(Base64.getEncoder()::encodeToString)
                .map("Basic %s"::formatted)
                .orElseThrow();
    }

    public List<DynDnsConfig> filterConfigWithDomainIpNotEquals(List<DynDnsConfig> dynDnsConfigs, String ip) {
        return dynDnsConfigs.stream()
                .map(c -> filterDomainsWithIpNotEquals(c, ip))
                .filter(c -> !c.getDomains().isEmpty())
                .toList();
    }

    public DynDnsConfig filterDomainsWithIpNotEquals(DynDnsConfig dynDnsConfig, String ip) {
        DynDnsConfig copy = dynDnsConfig.toBuilder().build();
        copy.setDomains(filterDomainsWithIpNotEquals(dynDnsConfig.getDomains(), ip));
        return copy;
    }

    public List<String> filterDomainsWithIpNotEquals(List<String> domains, String ip) {
        return domains.stream()
                .filter(d -> !isDomainIpEquals(d, ip))
                .toList();
    }

    public boolean isDomainIpEquals(String domain, String ip) {
        return Objects.equals(findDnsIp(domain), ip);
    }

    public String findDnsIp(String domain) {
        try {
            return InetAddress.getByName(domain).getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("Unable to resolve domain '%s'!".formatted(domain), e);
            return null;
        }
    }

    public void logMemoryInfo() {
        final Runtime runtime = Runtime.getRuntime();

        final long maxMemory = runtime.maxMemory();
        final long allocatedMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final long totalFreeMemory = freeMemory + maxMemory - allocatedMemory;

        logger.info("free memory: %dMB".formatted(toMB(freeMemory)));
        logger.info("allocated memory: %dMB".formatted(toMB(allocatedMemory)));
        logger.info("max memory: %dMB".formatted(toMB(maxMemory)));
        logger.info("total free memory: %dMB".formatted(toMB(totalFreeMemory)));
    }

    private long toMB(long b) {
        return b / (1024 * 1024);
    }
}
