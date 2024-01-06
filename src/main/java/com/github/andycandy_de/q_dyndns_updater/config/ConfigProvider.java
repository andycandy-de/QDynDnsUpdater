package com.github.andycandy_de.q_dyndns_updater.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConfigProvider {

    @ConfigProperty(name = ConfigConst.CONFIG_CONFIG_JSON)
    Optional<String> dynDnsJson;

    @ConfigProperty(name = ConfigConst.CONFIG_LOG_MEMORY_INFO)
    Optional<Boolean> logMemoryInfo;

    @ConfigProperty(name = ConfigConst.CONFIG_INTERVAL)
    Optional<Duration> interval;

    @ConfigProperty(name = ConfigConst.CONFIG_TIMES)
    Optional<Integer> times;

    @ConfigProperty(name = ConfigConst.CONFIG_SELF_CHECK, defaultValue = "false")
    Optional<Boolean> selfCheck;

    @ConfigProperty(name = ConfigConst.CONFIG_SELF_CHECK_URL)
    Optional<String> selfCheckUrl;

    @ConfigProperty(name = ConfigConst.CONFIG_UPDATE_URL)
    Optional<String> updateUrl;

    @ConfigProperty(name = ConfigConst.CONFIG_USERNAME)
    Optional<String> username;

    @ConfigProperty(name = ConfigConst.CONFIG_PASSWORD)
    Optional<String> password;

    @ConfigProperty(name = ConfigConst.CONFIG_DOMAINS)
    Optional<List<String>> domains;

    @ConfigProperty(name = ConfigConst.CONFIG_USER_AGENT)
    Optional<String> userAgent;

    @ConfigProperty(name = ConfigConst.CONFIG_DOMAINS_SINGLE_UPDATE)
    Optional<Boolean> domainsSingleUpdate;

    @ConfigProperty(name = ConfigConst.CONFIG_IP_RESOLVER_TYPE)
    Optional<String> ipResolverType;

    @ConfigProperty(name = ConfigConst.CONFIG_IP_RESOLVER_HTTP_GET_URL)
    Optional<String> ipResolverGetUrl;

    @ConfigProperty(name = ConfigConst.CONFIG_IP_RESOLVER_STATIC_IP)
    Optional<String> ipResolverStaticIp;

    @ConfigProperty(name = ConfigConst.CONFIG_IP_RESOLVER_COMMAND_COMMAND)
    Optional<String> ipResolverCommandCommand;

    @ConfigProperty(name = ConfigConst.CONFIG_IP_RESOLVER_COMMAND_TIMEOUT)
    Optional<Duration> ipResolverCommandTimeout;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Logger logger;

    public Config getConfig() {

        final Config config = this.createConfig();

        if (config.getLogMemoryInfo() == null) {
            config.setLogMemoryInfo(Boolean.FALSE);
        }
        if (config.getInterval() == null) {
            config.setInterval(Duration.of(5, ChronoUnit.MINUTES));
        }
        if (config.getTimes() == null) {
            config.setTimes(-1);
        }
        if (config.getSelfCheck() == null) {
            config.setSelfCheck(Boolean.FALSE);
        }
        if (config.getIpResolverCommandTimeout() == null) {
            config.setIpResolverCommandTimeout(Duration.of(5, ChronoUnit.MINUTES));
        }
        for (DynDnsConfig dynDnsConfig : config.getDynDnsConfigs()) {
            if (dynDnsConfig.getDomainsSingleUpdate() == null) {
                dynDnsConfig.setDomainsSingleUpdate(Boolean.FALSE);
            }
            if (dynDnsConfig.getDomains() == null) {
                dynDnsConfig.setDomains(List.of());
            }
        }

        return config;
    }

    private Config createConfig() {
        return dynDnsJson.map(File::new)
                .map(this::readJson)
                .orElse(this.createConfigFromProperties());
    }

    private Config createConfigFromProperties() {
        return Config.builder()
                .logMemoryInfo(logMemoryInfo.orElse(null))
                .interval(interval.orElse(null))
                .times(times.orElse(null))
                .selfCheck(selfCheck.orElse(null))
                .selfCheckUrl(selfCheckUrl.orElse(null))
                .dynDnsConfigs(createDynDnsConfigsFromProperties())
                .ipResolverType(ipResolverType.orElse(null))
                .ipResolverHttpGetUrl(ipResolverGetUrl.orElse(null))
                .ipResolverStaticIp(ipResolverStaticIp.orElse(null))
                .ipResolverCommandCommand(ipResolverCommandCommand.orElse(null))
                .ipResolverCommandTimeout(ipResolverCommandTimeout.orElse(null))
                .build();
    }

    private List<DynDnsConfig> createDynDnsConfigsFromProperties() {
        return List.of(
                DynDnsConfig.builder()
                        .updateUrl(updateUrl.orElse(null))
                        .username(username.orElse(null))
                        .password(password.orElse(null))
                        .domains(domains.map(List::copyOf).orElse(null))
                        .userAgent(userAgent.orElse(null))
                        .domainsSingleUpdate(domainsSingleUpdate.orElse(null))
                        .build()
        );
    }

    private Config readJson(File file) {
        try {
            return objectMapper.readValue(file, Config.class);
        } catch (IOException e) {
            logger.error("Unable to read config from file '%s'!".formatted(file.getAbsoluteFile()), e);
            return Config.builder().dynDnsConfigs(List.of()).build();
        }
    }

    @Produces
    @ApplicationScoped
    public Config produce() throws WrongConfigException {
        return validateConfig(getConfig());
    }

    private Config validateConfig(Config config) throws WrongConfigException {
        if (config.getSelfCheck() && config.getSelfCheckUrl() == null) {
            throw new WrongConfigException("Self check url must be set when self check is 'true'!");
        }
        if (config.getIpResolverType() == null || !List.of("static", "http_get", "command").contains(config.getIpResolverType())) {
            throw new WrongConfigException("Ip resolver type must be set to 'get', 'static' or 'command'!");
        }
        if ("static".equals(config.getIpResolverType()) && config.getIpResolverStaticIp() == null) {
            throw new WrongConfigException("Ip resolver static ip must be set when Ip resolver type is 'static'!");
        }
        if ("http_get".equals(config.getIpResolverType()) && config.getIpResolverHttpGetUrl() == null) {
            throw new WrongConfigException("Ip resolver http get url must be set when Ip resolver type is 'http_get'!");
        }
        if ("command".equals(config.getIpResolverType()) && config.getIpResolverCommandCommand() == null) {
            throw new WrongConfigException("Ip resolver command command must be set when Ip resolver type is 'command'!");
        }
        for (DynDnsConfig dynDnsConfig : config.getDynDnsConfigs()) {
            if (dynDnsConfig.getUpdateUrl() == null) {
                throw new WrongConfigException("Update url must be set!");
            }
            if (dynDnsConfig.getUsername() == null) {
                throw new WrongConfigException("Username must be set!");
            }
            if (dynDnsConfig.getPassword() == null) {
                throw new WrongConfigException("Password must be set!");
            }
            if (dynDnsConfig.getDomains().isEmpty()) {
                throw new WrongConfigException("Domains must be set!");
            }
        }
        return config;
    }
}
