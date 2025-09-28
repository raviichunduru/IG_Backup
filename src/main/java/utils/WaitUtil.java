package utils;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.enums.OrderType;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Supplier;

import static com.e2e_activecare.constants.FrameWorkConstants.getLoadTest_DeviceImport_CsvPath;
import static com.e2e_activecare.constants.FrameWorkConstants.getLoadTest_DeviceTransfer_CsvPath;
import static com.e2e_activecare.utils.FileUtil.*;
import static com.e2e_activecare.utils.UIUtil.hoverOverElement;

public final class WaitUtil {

    private WaitUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(WaitUtil.class);

    public static final ThreadLocal<WebDriverWait> WAIT = ThreadLocal.withInitial(() ->
            new WebDriverWait(PreDefinedActions.getDriver(), Duration.ofSeconds(TestConfig.getInt("waitTime")))
    );

    public static final ThreadLocal<WebDriverWait> SHORT_WAIT = ThreadLocal.withInitial(() ->
            new WebDriverWait(PreDefinedActions.getDriver(), Duration.ofSeconds(30))
    );

    public static final ThreadLocal<WebDriverWait> LONG_WAIT = ThreadLocal.withInitial(() ->
            new WebDriverWait(PreDefinedActions.getDriver(), Duration.ofSeconds(120))
    );

    public static void click(By locator) {
        try {
            WAIT.get().until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (Exception e1) {
            try {
                WebElement element = WAIT.get().until(ExpectedConditions.elementToBeClickable(locator));
                new Actions(PreDefinedActions.getDriver())
                        .moveToElement(element)
                        .click()
                        .perform();
            } catch (Exception e2) {
                try {
                    WebElement element = WAIT.get().until(ExpectedConditions.presenceOfElementLocated(locator));
                    ((JavascriptExecutor) PreDefinedActions.getDriver()).executeScript("arguments[0].click();", element);
                } catch (Exception e3) {
                    throw new RuntimeException("Failed to click element using all methods: " + locator, e3);
                }
            }
        }
    }

    public static void sendKeys(By locator, String value) {
        WAIT.get().until(ExpectedConditions.elementToBeClickable(locator)).sendKeys(value);
    }

    public static void sendKeysForMSP(By locator, String value) {
        WAIT.get().until(ExpectedConditions.presenceOfElementLocated(locator)).sendKeys(value);
    }

    public static void waitAndClear(By locator) {
        WAIT.get().until(ExpectedConditions.elementToBeClickable(locator)).clear();
    }

    public static WebElement waitTillDisplay(By locator) {
        return WAIT.get().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static boolean waitTillInvisibilityOfElement(By locator) {
        return SHORT_WAIT.get().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static WebElement waitTillDisplayForBellNotification(By locator) {
        return SHORT_WAIT.get().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    @SneakyThrows
    public static <T> void waitAndUploadCSVFileThroughSendKeys(By locator, OrderType orderType, Class<T> clazz) {
        WAIT.get()
                .until(ExpectedConditions.presenceOfElementLocated(locator))
                .sendKeys(getCSVFileForUpload(orderType, clazz).getAbsolutePath());
    }

    @SneakyThrows
    public static void waitAndUploadDeviceImportCSVFileForLoadTest(By locator) {
        String filePath = confirmIfFileExists(getLoadTest_DeviceImport_CsvPath());
        WAIT.get()
                .until(ExpectedConditions.presenceOfElementLocated(locator))
                .sendKeys(filePath);

        setFileNameForLoadTest(filePath);
    }

    @SneakyThrows
    public static void waitAndUploadDeviceTransferCSVFileForLoadTest(By locator) {
        String filePath = confirmIfFileExists(getLoadTest_DeviceTransfer_CsvPath());
        WAIT.get()
                .until(ExpectedConditions.presenceOfElementLocated(locator))
                .sendKeys(filePath);

        setFileNameForLoadTest(filePath);
    }

    public static void waitTillDocumentReady() {
        try {
            WAIT.get().until(driver -> {
                JavascriptExecutor jsExecutor = (JavascriptExecutor) PreDefinedActions.getDriver();
                String readyState = jsExecutor.executeScript("return document.readyState").toString();
                return "complete".equals(readyState);
            });
        } catch (Exception e) {
            throw new RuntimeException("Page did not load completely within the specified timeout", e);
        }
    }

    public static void waitForPeriodOf(long waitSeconds) {
        if (waitSeconds <= 0) {
            throw new IllegalArgumentException("Wait time must be greater than 0 seconds");
        }
        long waitMillis = waitSeconds * 1000;
        long endTime = System.currentTimeMillis() + waitMillis;
        while (System.currentTimeMillis() < endTime) {
        }
    }

    public static boolean waitTillCondition(Supplier<Boolean> condition, int timeoutSeconds) {
        try {
            new FluentWait<>(condition)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofSeconds(1))
                    .ignoring(Exception.class)
                    .until(Supplier::get);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean waitTillConditionWithRefresh(Supplier<Boolean> condition, int maxWaitSeconds) {
        long endTime = System.currentTimeMillis() + (maxWaitSeconds * 1000L);
        while (System.currentTimeMillis() < endTime) {
            try {
                if (condition.get()) return true;
            } catch (Exception ignored) {
            }
            PreDefinedActions.getDriver().navigate().refresh();
            waitTillDocumentReady();
            waitForPeriodOf(5);
        }
        return false;
    }

    public static String waitAndGetAttribute(By locator, String attribute) {
        try {
            WebElement element = WAIT.get().until(ExpectedConditions.elementToBeClickable(locator));
            return element.getDomAttribute(attribute);
        } catch (Exception e) {
            LOGGER.error("Failed to get attribute '{}' for locator: {}", attribute, locator, e);
            return null;
        }
    }

    public static boolean isElementDisplayed(By locator) {
        try {
            WebElement element = SHORT_WAIT.get().until(ExpectedConditions.elementToBeClickable(locator));
            return element != null && element.isDisplayed() && element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public static void clickByJS(By locator) {
        try {
            WebElement element = PreDefinedActions.getDriver().findElement(locator);
            ((JavascriptExecutor) PreDefinedActions.getDriver()).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to click element using JS: " + locator, e);
        }
    }

    public static void waitAndMouseHover (By locator) {
        WebElement element = WAIT.get().until(ExpectedConditions.visibilityOfElementLocated(locator));
        hoverOverElement(element);
    }

    public static String getText(By locator) {
        return WAIT.get().until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }
}
