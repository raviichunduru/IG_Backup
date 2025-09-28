package enums;

public enum Environment {
    US_STAGING("US-Staging"),
    EU_STAGING("EU-Staging"),
    US_STABLE("US-Stable"),
    EU_STABLE("EU-Stable");

    private final String displayName;

    Environment(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return displayName;
    }

    public static Environment fromString(String value) {
        for (Environment env : Environment.values()) {
            if (env.getValue().equalsIgnoreCase(value)) {
                return env;
            }
        }
        throw new IllegalArgumentException("Unsupported environment: " + value);
    }
}