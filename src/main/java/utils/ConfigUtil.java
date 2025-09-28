package utils;

import com.e2e_activecare.config.TestConfig;

public final class ConfigUtil {

    private ConfigUtil() {}
    public static final TestConfig CONFIG = TestConfig.loadProperties();
}
