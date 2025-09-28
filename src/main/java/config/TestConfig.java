package config;

import com.e2e_activecare.context.EnvironmentHolder;
import com.e2e_activecare.enums.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import static com.e2e_activecare.constants.FrameWorkConstants.*;
import static com.e2e_activecare.enums.Environment.*;

public final class TestConfig {

    private TestConfig() {}

    private static final Logger LOGGER = LogManager.getLogger(TestConfig.class);
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    public static synchronized TestConfig loadProperties() {
        if (!loaded) {
            String environmentProperty = System.getProperty("environment");
            if (environmentProperty == null) {
                throw new IllegalStateException("Environment system property is not set.");
            }

            Environment environment = Environment.fromString(environmentProperty);
            EnvironmentHolder.setEnvironment(environment);

            String configFilePath;
            if (environment == Environment.US_STAGING) {
                configFilePath = getE2eUSStagingConfigFilePath();
            } else if (environment == EU_STAGING) {
                configFilePath = getE2eEUStagingConfigFilePath();
            } else if (environment == US_STABLE) {
                configFilePath = getE2eUSStableConfigFilePath();
            } else if (environment == EU_STABLE) {
                configFilePath = getE2eEUStableConfigFilePath();
            } else {
                throw new IllegalStateException("Unsupported environment: " + environment);
            }

            try (InputStream input = new FileInputStream(configFilePath)) {
                properties.load(input);
                LOGGER.info("{} properties loaded for environment: {}", environment.getValue(), configFilePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load configuration from " + configFilePath, e);
            }
            loaded = true;
        }
        return null;
    }

    public static String get(String key) {
        loadProperties();
        String value = properties.getProperty(key);
        if (Objects.isNull(value)) throw new IllegalStateException(key + " is missing in config.");
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}