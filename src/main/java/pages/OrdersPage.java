package pages;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.context.EnvironmentHolder;
import com.e2e_activecare.enums.OrderType;
import com.e2e_activecare.enums.OrderStatus;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import com.e2e_activecare.pojo.DeviceRegistration;
import com.e2e_activecare.pojo.DeviceTransfer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static com.e2e_activecare.context.TestContextHolder.*;
import static com.e2e_activecare.enums.Environment.*;
import static com.e2e_activecare.enums.OrderType.*;
import static com.e2e_activecare.utils.BusinessLogicUtil.getFileNameToLookUnderOrdersPage;
import static com.e2e_activecare.utils.BusinessLogicUtil.isRunningTestPerfCUJ;
import static com.e2e_activecare.utils.PerformanceMetricUtil.endTimer;
import static com.e2e_activecare.utils.PerformanceMetricUtil.startTimer;
import static com.e2e_activecare.utils.DynamicXpathUtil.getDynamicXpath;
import static com.e2e_activecare.utils.WaitUtil.*;
import static com.e2e_activecare.enums.AttributeType.VALUE;
import static com.e2e_activecare.enums.OrderStatus.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.AzureUtil.triggerAzurePipelineToProcessOrder;

public final class OrdersPage {

    private OrdersPage() {}

    private static final OrdersPage ORDERS_PAGE_INSTANCE = new OrdersPage();
    private static final Logger LOGGER = LogManager.getLogger(OrdersPage.class);

    private static final String ROOT_EU_ADMIN_EMAIL = TestConfig.get("rootEuAdminEmail");
    private static final String ROOT_EU_ADMIN_PASSWORD = TestConfig.get("rootEuAdminPassword");

    private static final By ADD_ORDER_BUTTON = By.xpath("//span[text()='Add']/parent::button");
    private static final By NEW_ORDER_ACTIVE_CARE_LINK = By.xpath("//li[@id='wex-menu-list-item' and .//span[text()='New Order - Active Care']]");
    private static final By NEW_ORDER_PREMIUM_LINK = By.xpath("//li[@id='wex-menu-list-item' and .//span[text()='New Order - Premium']]");
    private static final By NEW_ORDER_PREMIUM_PLUS_LINK = By.xpath("//li[@id='wex-menu-list-item' and .//span[text()='New Order - Premium Plus']]");
    private static final By NEW_ORDER_OUT_Of_BAND_REMEDIATION_LINK = By.xpath("//li[@id='wex-menu-list-item' and .//span[text()='New Order - Out Of Band Remediation']]");
    private static final By DEVICE_TRANSFER_LINK = By.xpath("//li[@id='wex-menu-list-item' and .//span[text()='Device Transfer']]");
    private static final By UPLOAD_FILE = By.cssSelector("input[type='file']");
    private static final By IMPORT_ORDER_BUTTON = By.xpath("//button[@id='import-order']");
    private static final By IMPORT_SUCCESSFUL_MESSAGE = By.xpath("//div[text()='File Uploaded Successfully']");
    private static final By CLEAR_LINK = By.xpath("//button[@aria-label='Clear']");
    private static final By SEARCH_FILTER = By.xpath("//input[@id='search']");
    private static final By CALENDAR_FILTER = By.xpath("//input[@id='date_picker']");
    private static final String UPLOADED_FILE_NAME_XPATH = "//a[text()='%s']";
    private static final By CLEAR_FILTERS_LINK = By.xpath("//button[@aria-label='Clear Filters']");

    public static OrdersPage getInstance() {
        return ORDERS_PAGE_INSTANCE;
    }

    private OrdersPage clickAddOrderButton() {
        click(ADD_ORDER_BUTTON);
        return this;
    }

    private OrdersPage clickLinkBasedOnOrderType(OrderType orderType) {
        EnumMap<OrderType, By> orderTypeLinkMap = new EnumMap<>(OrderType.class);
        orderTypeLinkMap.put(ACTIVE_CARE, NEW_ORDER_ACTIVE_CARE_LINK);
        orderTypeLinkMap.put(PREMIUM, NEW_ORDER_PREMIUM_LINK);
        orderTypeLinkMap.put(PREMIUM_PLUS, NEW_ORDER_PREMIUM_PLUS_LINK);
        orderTypeLinkMap.put(OUT_OF_BAND_REMEDIATION, NEW_ORDER_OUT_Of_BAND_REMEDIATION_LINK);

        By locator = orderTypeLinkMap.get(orderType);
        if (locator == null) {
            throw new IllegalArgumentException("Unknown order type: " + orderType);
        }

        click(locator);
        return this;
    }

    private OrdersPage clickDeviceTransfer() {
        click(DEVICE_TRANSFER_LINK);
        return this;
    }

    private OrdersPage uploadCsvFile(OrderType orderType, Class<?> clazz) {
        waitAndUploadCSVFileThroughSendKeys(UPLOAD_FILE, orderType, clazz);
        return this;
    }

    private OrdersPage uploadDeviceImportCsvFileForLoadTest() {
        waitAndUploadDeviceImportCSVFileForLoadTest(UPLOAD_FILE);
        return this;
    }

    private OrdersPage uploadDeviceTransferCsvFileForLoadTest() {
        waitAndUploadDeviceTransferCSVFileForLoadTest(UPLOAD_FILE);
        return this;
    }

    private void clickImportOrderButton() {
        click(IMPORT_ORDER_BUTTON);
    }

    public OrdersPage uploadCSV() {
        OrderType[] orderTypes = getCurrentOrderTypes();
        int count = getCsvUploadCount();

        OrderType orderType = orderTypes[count];

        clickAddOrderButton()
                .clickLinkBasedOnOrderType(orderType)
                .uploadCsvFile(orderType, DeviceRegistration.class)
                .clickImportOrderButton();

        setCsvUploadCount(count + 1);
        return this;
    }

    public OrdersPage uploadDeviceImportCSV_WithLoad() {
        OrderType[] orderTypes = getCurrentOrderTypes();
        int count = getCsvUploadCount();

        OrderType orderType = orderTypes[count];

        clickAddOrderButton()
                .clickLinkBasedOnOrderType(orderType)
                .uploadDeviceImportCsvFileForLoadTest()
                .clickImportOrderButton();

        setCsvUploadCount(count + 1);
        return this;
    }

    public OrdersPage uploadDeviceTransferCSV_WithLoad() {
        clickAddOrderButton()
                .clickDeviceTransfer()
                .uploadDeviceTransferCsvFileForLoadTest()
                .clickImportOrderButton();
        return this;
    }

    public OrdersPage uploadCSV_ToProcess_DeviceTransfer_OrderType() {
        clickAddOrderButton()
                .clickDeviceTransfer()
                .uploadCsvFile(DEVICE_TRANSFER, DeviceTransfer.class)
                .clickImportOrderButton();
        return this;
    }

    public OrdersPage assertUploadSuccessful() {
        softly().assertThat(waitTillDisplay(IMPORT_SUCCESSFUL_MESSAGE).isDisplayed())
                .withFailMessage("Upload success message not displayed")
                .isTrue();

        LOGGER.info("CSV file uploaded successfully");
        return this;
    }

    private static void clearSearchFilterIfApplied() {
        Map<By, String> filters = Map.of(
                SEARCH_FILTER, "search",
                CALENDAR_FILTER, "calendar"
        );

        filters.forEach((locator, name) -> {
            String filterValue = waitAndGetAttribute(locator, VALUE.getValue());
            if (Objects.nonNull(filterValue) && !filterValue.isBlank()) {
                click(CLEAR_LINK);
                LOGGER.info("{} filter cleared", name);
            }
        });
    }

    private static void clearFiltersIfApplied() {
        waitForPeriodOf(5);
        if (isElementDisplayed(CLEAR_FILTERS_LINK)) {
            click(CLEAR_FILTERS_LINK);
        }
    }

    private WebElement getUploadedFileElement() {
        String fileName = getFileNameToLookUnderOrdersPage();
        return waitTillDisplay(getDynamicXpath(UPLOADED_FILE_NAME_XPATH, fileName));
    }

    @SneakyThrows
    public OrdersPage assertUploadedFileIsDisplayedUnderOrders() {
        if(isRunningTestPerfCUJ()){
            startTimer("file_display_in_orders");
        }

        switchToEUPortalIfTestRunningInEUEnvironment();
        clearFiltersIfApplied();
        waitForPeriodOf(5);

        String fileName = getFileNameToLookUnderOrdersPage();
        sendKeys(SEARCH_FILTER,fileName);
        waitForPeriodOf(5);

        By uploadedFileLocator = getDynamicXpath(UPLOADED_FILE_NAME_XPATH, fileName);

        boolean isDisplayed = waitTillConditionWithRefresh(() ->
                PreDefinedActions.getDriver().findElements(uploadedFileLocator).stream()
                        .anyMatch(WebElement::isDisplayed), TestConfig.getInt("waitTime")
        );

        assertThat(isDisplayed)
                .withFailMessage("Uploaded file '%s' not displayed in Orders page within maximum waiting period", fileName)
                .isTrue();

        if(isRunningTestPerfCUJ()){
            endTimer("file_display_in_orders");
        }
        return this;
    }

    private static void switchToEUPortalIfTestRunningInEUEnvironment() throws Exception {
        if (EnvironmentHolder.getEnvironment().equals(EU_STABLE) || EnvironmentHolder.getEnvironment().equals(EU_STAGING)) {
            HomePage.getInstance()
                    .signOut()
                    .loadDASSPortal();

            LandingPage.getInstance()
                    .clickSignInButton()
                    .signIn(ROOT_EU_ADMIN_EMAIL,ROOT_EU_ADMIN_PASSWORD)
                    .navigateToOrdersPage();
        }
    }

    private WebElement getStatusCellForUploadedFile() {
        return getUploadedFileElement().findElement(By.xpath("./ancestor::tr/td[4]"));
    }

    public OrdersPage assertOrderStatusIs(OrderStatus expectedStatus) {
        boolean isStatusUpdated = waitTillConditionWithRefresh(() -> {
            String actualStatus = getStatusCellForUploadedFile().getText();
            return actualStatus.equals(expectedStatus.getValue());
        }, TestConfig.getInt("waitTime"));

        if (!isStatusUpdated) {
            String actualStatus = getStatusCellForUploadedFile().getText();
            assertThat(actualStatus)
                    .withFailMessage("Expected status: '%s', but found: '%s'", expectedStatus.getValue(), actualStatus)
                    .isEqualTo(expectedStatus.getValue());
        }
        LOGGER.info("Order Status is as expected : {}",expectedStatus);
        return this;
    }

    private boolean attemptCronJobAndCheckStatus(int attempt) {
        triggerAzurePipelineToProcessOrder();
        LOGGER.info("Cron job completed on attempt #{}", attempt);

        return waitTillConditionWithRefresh(() -> {
            clearFiltersIfApplied();
            String fileName = getFileNameToLookUnderOrdersPage();
            sendKeys(SEARCH_FILTER,fileName);
            waitForPeriodOf(5);

            String statusText = getStatusCellForUploadedFile().getText();
            return COMPLETED.getValue().equals(statusText);
        },240);
    }

    public OrdersPage assert_OrderStatus_IsChangedTo_Completed_AfterCronJobRun() {
        if(isRunningTestPerfCUJ())  {
            startTimer("cron_job_order_processing");
        }
        final int maxAttempts = 5;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (attemptCronJobAndCheckStatus(attempt)) {
                    LOGGER.info("Order Status changed to 'COMPLETED'");
                    if(isRunningTestPerfCUJ()) {
                        endTimer("cron_job_order_processing");
                        startTimer("ac_plan_allocation");
                    }
                    return this;
                }
            } catch (Exception e) {
                LOGGER.error("Error during Azure pipeline trigger or order status check (attempt #{})", attempt, e);
            }
        }
        throw new AssertionError("FAIL: Order Status did not change to 'COMPLETED' after " + maxAttempts + " cron job runs.");
    }

    public OrderDetailsPage navigateToOrderDetailPage() {
        getUploadedFileElement().click();
        return OrderDetailsPage.getInstance();
    }
}
