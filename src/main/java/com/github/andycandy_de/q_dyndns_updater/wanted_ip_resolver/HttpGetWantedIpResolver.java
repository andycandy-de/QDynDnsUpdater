package com.github.andycandy_de.q_dyndns_updater.wanted_ip_resolver;

import com.github.andycandy_de.q_dyndns_updater.Helper;
import com.github.andycandy_de.q_dyndns_updater.config.Config;
import com.github.andycandy_de.q_dyndns_updater.http_client.IGetStringHttp;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HttpGetWantedIpResolver implements IWantedIpResolver {

    @Inject
    Logger logger;

    @Inject
    Helper helper;

    @Inject
    Config config;

    @Override
    public String resolveWantedIp() {
        try {
            return helper.createRestClient(config.getIpResolverHttpGetUrl(), IGetStringHttp.class).getString();
        } catch (Exception e) {
            logger.warn("Unable to get wanted ip with the url '%s'.".formatted(config.getIpResolverHttpGetUrl()), e);
        }
        return null;
    }

    @Override
    public String getType() {
        return "http_get";
    }
}
