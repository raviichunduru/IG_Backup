package context;

import com.e2e_activecare.enums.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnvironmentHolder {
    private static Environment environment;
    private static final Logger LOGGER = LogManager.getLogger(EnvironmentHolder.class);

    private EnvironmentHolder() {}

    public static void setEnvironment(Environment env) {
        if (environment != null) {
            throw new IllegalStateException("Environment is already set and cannot be changed.");
        }
        environment = env;
        LOGGER.info("Environment set to: {}", env.getValue());
    }

    public static Environment getEnvironment() {
        if (environment == null) {
            throw new IllegalStateException("Environment is not set. Please initialize it before accessing.");
        }
        return environment;
    }

    public static String getEnvironmentName() {
        if (environment == null) {
            throw new IllegalStateException("Environment is not set. Please initialize it before accessing.");
        }

        String rawEnvironmentName = environment.getValue();
        switch (rawEnvironmentName) {
            case "US-Stable":
                return "usdevms";
            case "EU-Stable":
                return "eudevms";
            case "EU-Staging":
                return "eustagingms";
            case "US-Staging":
                return "usstagingms";
            default:
                throw new IllegalArgumentException("Unsupported environment: " + rawEnvironmentName);
        }
    }

    public static String getHost() {
        if (environment == null) {
            throw new IllegalStateException("Environment is not set. Please initialize it before accessing.");
        }

        String rawEnvironmentName = environment.getValue();
        switch (rawEnvironmentName) {
            case "US-Stable":
                return "http://internal.usdev.daas.hppipeline.com";
            case "EU-Stable":
                return "http://internal.eudev.daas.hppipeline.com";
            case "EU-Staging":
                return "http://internal.eustaging.hpdaas.com";
            case "US-Staging":
                return "http://internal.usstaging.hpdaas.com";
            default:
                throw new IllegalArgumentException("Unsupported environment: " + rawEnvironmentName);

        }
    }
}