package com.github.andycandy_de.q_dyndns_updater.wanted_ip_resolver;

import com.github.andycandy_de.q_dyndns_updater.config.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WantedIpResolver implements IWantedIpResolver {

    @Inject
    StaticWantedIpResolver staticWantedIpResolver;

    @Inject
    HttpGetWantedIpResolver httpGetWantedIpResolver;

    @Inject
    CommandWantedIpResolver commandWantedIpResolver;

    @Inject
    Config config;

    @Override
    public String resolveWantedIp() {
        return switch (config.getIpResolverType()) {
            case "static" -> staticWantedIpResolver.resolveWantedIp();
            case "http_get" -> httpGetWantedIpResolver.resolveWantedIp();
            case "command" -> commandWantedIpResolver.resolveWantedIp();
            default -> null;
        };
    }
}
