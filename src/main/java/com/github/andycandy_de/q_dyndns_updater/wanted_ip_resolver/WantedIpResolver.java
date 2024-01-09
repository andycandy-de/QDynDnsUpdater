package com.github.andycandy_de.q_dyndns_updater.wanted_ip_resolver;

import com.github.andycandy_de.q_dyndns_updater.config.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Objects;

@ApplicationScoped
public class WantedIpResolver {

    @Inject
    @Any
    Instance<IWantedIpResolver> wantedIpResolvers;

    @Inject
    Config config;

    public String resolveWantedIp() {
        return wantedIpResolvers.stream()
                .filter(this::filterType)
                .map(IWantedIpResolver::resolveWantedIp)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No valid type found! %s".formatted(config.getIpResolverType())));
    }

    private boolean filterType(IWantedIpResolver wantedIpResolver) {
        return Objects.equals(wantedIpResolver.getType(), config.getIpResolverType());
    }
}
