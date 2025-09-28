package enums;

public enum IssueCategory {

    BATTERY("Manually Created - Battery");

    private final String displayName;

    IssueCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return displayName;
    }
}
