package com.github.andycandy_de.q_dyndns_updater.self_checker.http;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/")
public interface ISelfCheckHttp {

    @GET
    @Path("/{requestUuid}")
    String selfCheck(@PathParam("requestUuid") String uuid);
}
