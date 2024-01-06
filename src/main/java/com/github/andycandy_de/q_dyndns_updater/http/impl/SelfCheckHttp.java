package com.github.andycandy_de.q_dyndns_updater.http.impl;

import com.github.andycandy_de.q_dyndns_updater.CheckUuidHolder;
import com.github.andycandy_de.q_dyndns_updater.http.ISelfCheckHttp;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.Optional;

public class SelfCheckHttp implements ISelfCheckHttp {

    @Inject
    Logger logger;

    @Inject
    CheckUuidHolder checkUuidHolder;

    @Override
    public String selfCheck(String uuid) {
        logger.info("Receiving self check with requestUuid '%s'!".formatted(uuid));
        return Optional.ofNullable(uuid)
                .map(checkUuidHolder::findResponseUuid)
                .orElseThrow(NotFoundException::new);
    }
}
