package com.github.andycandy_de.q_dyndns_updater.self_checker.http.impl;

import com.github.andycandy_de.q_dyndns_updater.self_checker.SelfCheckUuidHolder;
import com.github.andycandy_de.q_dyndns_updater.self_checker.http.ISelfCheckHttp;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.Optional;

public class SelfCheckHttp implements ISelfCheckHttp {

    @Inject
    Logger logger;

    @Inject
    SelfCheckUuidHolder selfCheckUuidHolder;

    @Override
    public String selfCheck(String uuid) {
        logger.info("Receiving self check with requestUuid '%s'!".formatted(uuid));
        return Optional.ofNullable(uuid)
                .map(selfCheckUuidHolder::findResponseUuid)
                .orElseThrow(NotFoundException::new);
    }
}
