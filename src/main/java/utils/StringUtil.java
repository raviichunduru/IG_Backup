package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class StringUtil {

    private StringUtil() {}

    public static List<String> toList(String string) {
        if (string == null || string.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(string.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
