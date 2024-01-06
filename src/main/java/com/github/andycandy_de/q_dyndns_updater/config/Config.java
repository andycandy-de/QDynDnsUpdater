package com.github.andycandy_de.q_dyndns_updater.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Builder
@Data
public class Config {

    private Boolean logMemoryInfo;

    private Duration interval;

    private Integer times;

    private Boolean selfCheck;

    private String selfCheckUrl;

    private List<DynDnsConfig> dynDnsConfigs;

    private String ipResolverType;

    private String ipResolverHttpGetUrl;

    private String ipResolverStaticIp;

    private String ipResolverCommandCommand;

    private Duration ipResolverCommandTimeout;
}
