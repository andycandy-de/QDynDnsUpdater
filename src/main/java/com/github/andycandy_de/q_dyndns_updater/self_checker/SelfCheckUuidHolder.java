package com.github.andycandy_de.q_dyndns_updater.self_checker;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SelfCheckUuidHolder {

    private Map<String, String> uuidMap = new ConcurrentHashMap<>();

    public void addUuid(String requestUuid, String responseUuid) {
        uuidMap.put(requestUuid, responseUuid);
    }

    public void removeUuid(String requestUuid) {
        uuidMap.remove(requestUuid);
    }

    public String findResponseUuid(String requestUuid) {
        return uuidMap.get(requestUuid);
    }
}
