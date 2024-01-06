package com.github.andycandy_de.q_dyndns_updater.http_client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;

@Path("/")
public interface IDynDnsUpdate {

    @GET
    String update(@HeaderParam("Authorization") String authorization, @HeaderParam("User-Agent") String userAgent, @QueryParam("hostname") List<String> domains, @QueryParam("myip") String ip);
}
