package com.github.andycandy_de.q_dyndns_updater.config;

import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Builder
@Data
public class Config {

    private Boolean logMemoryInfo;

    private Duration interval;

    private Integer times;

    private Boolean selfCheck;

    private URI selfCheckUrl;

    private List<DynDnsConfig> dynDnsConfigs;

    private String ipResolverType;

    private URI ipResolverHttpGetUrl;

    private String ipResolverStaticIp;

    private String ipResolverCommandCommand;

    private Duration ipResolverCommandTimeout;
}
