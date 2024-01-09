package com.github.andycandy_de.q_dyndns_updater.wanted_ip_resolver;

import com.github.andycandy_de.q_dyndns_updater.config.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.*;

@ApplicationScoped
public class CommandWantedIpResolver implements IWantedIpResolver {

    @Inject
    Logger logger;

    @Inject
    Config config;

    @Override
    public String resolveWantedIp() {
        return executeCommandWithTimeout(config.getIpResolverCommandCommand(), config.getIpResolverCommandTimeout());
    }

    private String executeCommandWithTimeout(String command, Duration duration) {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            return executorService.submit(() -> executeCommand(command)).get(duration.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Unable to get wanted ip!", e);
            return null;
        }
    }

    private String executeCommand(String command) throws IOException {
        final Process process = exec(command);

        final String error = readFirstLine(process.getErrorStream());
        if (error != null) {
            logger.error("Error occurred from command '%s'! error: '%s'".formatted(command, error));
        }

        final String result = Optional.ofNullable(readFirstLine(process.getInputStream()))
                .map(String::trim)
                .filter(String::isEmpty)
                .orElse(null);

        if (result == null) {
            logger.error("Unable to get result from command '%s'!".formatted(command));
        }

        return result;
    }

    private String readFirstLine(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.readLine();
        }
    }

    private Process exec(String command) throws IOException {
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdArray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdArray[i] = st.nextToken();
        return Runtime.getRuntime().exec(cmdArray);
    }

    @Override
    public String getType() {
        return "command";
    }
}
