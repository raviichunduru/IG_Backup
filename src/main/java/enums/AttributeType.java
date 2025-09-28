package enums;

public enum AttributeType {
    VALUE("value"),
    TITLE("title");

    private final String displayName;

    AttributeType(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return displayName;
    }
}

