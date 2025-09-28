package enums;

public enum OrderStatus {
    NEW("New"),
    COMPLETED("Completed");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return displayName;
    }
}
