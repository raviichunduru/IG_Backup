package utils;

import org.openqa.selenium.By;

public final class DynamicXpathUtil {

    private DynamicXpathUtil () {}

    public static By getDynamicXpath(String originalString, String replaceWith) {
        return By.xpath(String.format(originalString,replaceWith));
    }
}
