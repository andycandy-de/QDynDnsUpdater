package com.github.andycandy_de.q_dyndns_updater;

import io.quarkus.runtime.Startup;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AppStarter {

    @Inject
    EventBus bus;

    @Startup
    void startup() {
        bus.publish("start", null);
    }
}
