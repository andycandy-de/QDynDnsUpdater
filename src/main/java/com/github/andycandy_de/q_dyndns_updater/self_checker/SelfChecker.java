package com.github.andycandy_de.q_dyndns_updater.self_checker;

import com.github.andycandy_de.q_dyndns_updater.Helper;
import com.github.andycandy_de.q_dyndns_updater.config.Config;
import com.github.andycandy_de.q_dyndns_updater.self_checker.http.ISelfCheckHttp;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class SelfChecker {

    @Inject
    Logger logger;

    @Inject
    SelfCheckUuidHolder selfCheckUuidHolder;

    @Inject
    Helper helper;

    @Inject
    Config config;

    public boolean selfCheck() {
        final String requestUuid = UUID.randomUUID().toString();
        final String responseUuid = UUID.randomUUID().toString();
        logger.info("Sending self check with requestUuid='%s' and responseUuid='%s'".formatted(requestUuid, responseUuid));
        try {
            selfCheckUuidHolder.addUuid(requestUuid, responseUuid);
            if (Objects.equals(responseUuid, getCheckUuid(requestUuid))) {
                logger.info("Self check was successful!");
                return true;
            }
        } catch (Exception ignored) {
            //ignore
        } finally {
            selfCheckUuidHolder.removeUuid(requestUuid);
        }
        logger.info("Self check was not successful!");
        return false;
    }

    private String getCheckUuid(String requestUuid) {
        return helper.createRestClient(config.getSelfCheckUrl(), ISelfCheckHttp.class).selfCheck(requestUuid);
    }
}
