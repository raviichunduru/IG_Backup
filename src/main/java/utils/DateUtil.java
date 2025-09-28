package utils;

import com.e2e_activecare.enums.TimeStampFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private DateUtil() {}

    public static String getCurrentTimestamp(TimeStampFormat format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format.getFormat());
        return LocalDateTime.now().format(formatter);
    }

    public static String formatDate(String date, TimeStampFormat fromFormat, TimeStampFormat toFormat) {
        DateTimeFormatter fromFormatter = DateTimeFormatter.ofPattern(fromFormat.getFormat());
        DateTimeFormatter toFormatter = DateTimeFormatter.ofPattern(toFormat.getFormat());

        if (fromFormat.getFormat().contains("HH:mm:ss")) {
            LocalDateTime parsedDateTime = LocalDateTime.parse(date, fromFormatter);
            return parsedDateTime.format(toFormatter);
        } else {
            LocalDate parsedDate = LocalDate.parse(date, fromFormatter);
            return parsedDate.format(toFormatter);
        }
    }
}