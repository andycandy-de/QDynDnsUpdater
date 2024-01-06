package com.github.andycandy_de.q_dyndns_updater.wanted_ip_resolver;

import com.github.andycandy_de.q_dyndns_updater.config.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StaticWantedIpResolver implements IWantedIpResolver {

    @Inject
    Config config;

    @Override
    public String resolveWantedIp() {
        return config.getIpResolverStaticIp();
    }
}
