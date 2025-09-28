package utils;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.OrderType;
import com.e2e_activecare.pojo.DataHolderForDBValidations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.e2e_activecare.enums.OrderType.*;
import static com.e2e_activecare.pages.DeviceDetailsPage.*;
import static com.e2e_activecare.utils.WaitUtil.*;
import static com.e2e_activecare.utils.WaitUtil.getText;

public final class UIUtil {

    private UIUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(UIUtil.class);

    private static final ThreadLocal<Actions> ACTIONS = ThreadLocal.withInitial(() ->
            new Actions(PreDefinedActions.getDriver()));

    public static String clean(String input) {
        return input == null ? null : input
                .replaceAll("[\\u200B\\u200C\\u200D\\uFEFF]", "") // invisible Unicode
                .replace('\u00A0', ' ') // non-breaking space to regular space
                .trim(); // remove leading/trailing whitespace
    }

    public static boolean isDisplayed(By locator) {
        try {
            WebDriverWait wait = new WebDriverWait(PreDefinedActions.getDriver(), Duration.ofSeconds(30));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return element.isDisplayed() && element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public static void hoverOverElement(WebElement element) {
        ACTIONS.get()
                .moveToElement(element)
                .perform();
    }

    public static void scrollAndClick(By locator) {
        final int maxScrolls = 10;
        int scrollCount = 0;
        boolean isClicked = false;
        WebDriver driver = PreDefinedActions.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        Actions actions = new Actions(driver);

        while (scrollCount < maxScrolls) {
            try {
                WebElement element = driver.findElement(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);

                wait.until(ExpectedConditions.elementToBeClickable(element));

                try {
                    actions.moveToElement(element).click().perform();
                    isClicked = true;
                    break;
                } catch (Exception e) {
                    LOGGER.warn("Actions class click failed. Error: {}", e.getMessage());
                }
            } catch (Exception e) {
                LOGGER.warn("Exception occurred while finding or interacting with the element: {}", e.getMessage());
            }

            LOGGER.info("Scrolling down by 200 pixels. Attempt: {}", scrollCount + 1);
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 200);");
            scrollCount++;
            waitForPeriodOf(2);
        }
        if (!isClicked) {
            throw new RuntimeException("Element not visible/clickable after scrolling: " + locator);
        }
    }

    public static WebElement scrollAndFindElement(By locator) {
        final int maxScrolls = 10;
        int scrollCount = 0;
        WebDriver driver = PreDefinedActions.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement element;

        while (scrollCount < maxScrolls) {
            try {
                element = driver.findElement(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);

                wait.until(ExpectedConditions.visibilityOf(element));
                return element;
            } catch (Exception ignored) {
            }
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 200);");
            scrollCount++;
            WaitUtil.waitForPeriodOf(5);
        }
        throw new RuntimeException("Element not visible after scrolling: " + locator);
    }

    public static void handleOptionalNotification(By locator) {
        try {
            if (isElementDisplayed(locator)) {
                click(locator);
                LOGGER.info("Notification closed successfully");
            } else {
                LOGGER.info("Notification not displayed, no action needed");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle optional notification: {}", e.getMessage());
        }
    }

    public static void fetchingDeviceIdFromURL() {

        String currentUrl = PreDefinedActions.getDriver().getCurrentUrl();

        String[] parts = currentUrl.split("/");
        String deviceId = parts[parts.length - 1];

        DataHolderForDBValidations dataHolder = TestContextHolder.getDataHolder();
        dataHolder.setDeviceId(deviceId);
        //LOGGER.info("Device ID set in DataHolderForDBValidations: {}", dataHolder.getDeviceId());
    }

    public static void fetchPlanEnrollmentStartEndDates(OrderType orderType) {
        if (orderType == ACTIVE_CARE || orderType == PREMIUM || orderType == PREMIUM_PLUS) {
            fetchActiveCareEnrollmentStartEndDates();
        } else if (orderType == OUT_OF_BAND_REMEDIATION) {
            fetchRemoteCareEnrollmentStartEndDates();
        } else {
            throw new IllegalArgumentException("Unknown order type for enrollment dates: " + orderType);
        }
    }

    public static void fetchActiveCareEnrollmentStartEndDates() {

        String acEnrollmentStartDate = extractDate(getText(ACTIVE_CARE_ENROLLMENT_START_DATE));
        String acEnrollmentEndDate = extractDate(getText(ACTIVE_CARE_ENROLLMENT_END_DATE));

        LOGGER.info("AC Plan Start Date: {}", acEnrollmentStartDate);
        LOGGER.info("AC Plan End Date: {}", acEnrollmentEndDate);

        DataHolderForDBValidations dataHolder = TestContextHolder.getDataHolder();
        dataHolder.setAcPlanStartDate(acEnrollmentStartDate);
        dataHolder.setAcPlanEndDate(acEnrollmentEndDate);
    }

    public static void fetchRemoteCareEnrollmentStartEndDates() {

        String rcEnrollmentStartDate = extractDate(getText(REMOTE_CARE_ENROLLMENT_START_DATE));
        String rcEnrollmentEndDate = extractDate(getText(REMOTE_CARE_ENROLLMENT_END_DATE));

        LOGGER.info("RC Plan Start Date: {}", rcEnrollmentStartDate);
        LOGGER.info("RC Plan End Date: {}", rcEnrollmentEndDate);

        DataHolderForDBValidations dataHolder = TestContextHolder.getDataHolder();
        dataHolder.setRcPlanStartDate(rcEnrollmentStartDate);
        dataHolder.setRcPlanEndDate(rcEnrollmentEndDate);
    }

    private static String extractDate(String input) {
        String[] parts = input.split(" ");

        // "dd/MM/yyyy"
        for (String part : parts) {
            if (part.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return part;
            }
        }

        // "d MMM yyyy" or "dd MMM yyyy"
        for (int i = 0; i < parts.length - 2; i++) {
            String candidate = parts[i] + " " + parts[i+1] + " " + parts[i+2];
            if (candidate.matches("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")) {
                return candidate;
            }
        }
        return null;
    }
}
