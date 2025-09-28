package enums;

public enum DeviceStatus {
    ACTIVE("Active"),
    READY("Ready"),
    ERROR("Error"),
    INACTIVE("Inactive"),
    NONE("None");

    private final String displayName;

    DeviceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return displayName;
    }
}
