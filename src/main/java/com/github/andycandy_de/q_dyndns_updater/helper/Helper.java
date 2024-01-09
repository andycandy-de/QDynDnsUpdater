package com.github.andycandy_de.q_dyndns_updater.helper;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Base64;
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

    public String findDnsIp(String domain) {
        try {
            return InetAddress.getByName(domain).getHostAddress();
        } catch (UnknownHostException e) {
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
