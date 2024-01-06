package com.github.andycandy_de.q_dyndns_updater.http_client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public interface IGetStringHttp {

    @GET
    String getString();
}
