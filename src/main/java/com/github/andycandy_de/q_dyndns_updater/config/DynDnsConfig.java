package com.github.andycandy_de.q_dyndns_updater.config;

import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.util.List;

@Builder(toBuilder = true)
@Data
public class DynDnsConfig {

    private URI updateUrl;

    private String username;

    private String password;

    private List<String> domains;

    private String userAgent;

    private Boolean domainsSingleUpdate;
}
