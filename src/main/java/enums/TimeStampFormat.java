package enums;

import lombok.Getter;

@Getter
public enum TimeStampFormat {

    DDMMYYYY_HHMMSS("ddMMyyyy_HHmmss"),
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd_HH-mm-ss"),
    ISO_DATE("yyyy-MM-dd"),
    YYYY_MM_DD_HH_MM_SS_SSS("yyyy-MM-dd_HH-mm-ss-SSS"),
    DB_DATE_FORMAT("yyyy-MM-dd HH:mm:ss"),
    DB_DATE_ONLY_FORMAT("yyyy-MM-dd"),
    UI_DATE_FORMAT_FOR_CSV_DATES("d-MMM-yy"),
    UI_DATE_FORMAT_FOR_PLAN_ENROLL("MM/dd/yyyy"),
    UI_DATE_FORMAT_FOR_SERVICE_DATE("d-MMM-yy"),
    WXP_UI_DATE_FORMAT_FOR_PLAN_ENROLL("d MMM yyyy");

    private final String format;

    TimeStampFormat(String format) {
        this.format = format;
    }
}