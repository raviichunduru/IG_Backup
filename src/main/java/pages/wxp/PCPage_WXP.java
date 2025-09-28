package pages.wxp;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.pojo.context.SerialNumberHolder;
import com.e2e_activecare.enums.DeviceStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import static com.e2e_activecare.utils.BusinessLogicUtil.isRunningTestPerfCUJ;
import static com.e2e_activecare.utils.PerformanceMetricUtil.endTimer;
import static com.e2e_activecare.utils.PerformanceMetricUtil.startTimer;
import static com.e2e_activecare.utils.APIUtil.fetchCountOfDevicesWith_NAPlan_ForWxpTenant;
import static com.e2e_activecare.utils.AssertUtil.assertAll;
import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.DynamicXpathUtil.getDynamicXpath;
import static com.e2e_activecare.utils.ShellUtil.getCsvForDeviceUnroll;
import static com.e2e_activecare.utils.UIUtil.*;
import static com.e2e_activecare.utils.WaitUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public final class PCPage_WXP {

    private PCPage_WXP() {}

    private static final PCPage_WXP PC_PAGE_WXP_INSTANCE = new PCPage_WXP();
    private static final Logger LOGGER = LogManager.getLogger(PCPage_WXP.class);

    private String serialNumber = "";
    private static By xpathForSerialNumberLink = null;

    private static final By CLEAR_FILTERS_LINK = By.xpath("//button[@aria-label='Clear Filters']");
    private static final By SERIAL_NUMBER_FILTER = By.xpath("//input[@id='serialNumber']");
    private static final String SERIAL_NUMBER_LINK = "//a[text()='%s']";
    private static final String SERIAL_NUMBER_STATUS = "//tr[.//a[text()='%s']]/td[4]//div[2]";
    private static final By DEVICE_COUNT = By.xpath("//button[@aria-label='Select page size']/following-sibling::span[starts-with(text(), 'of')]");
    private static final By PAGE_SIZE_BUTTON = By.xpath("//button[@aria-label='Select page size']");
    private static final By PAGE_SIZE_100 = By.xpath("//span[text()='100']/parent::button[@role='menuitem']");
    private static final By PLANS_DROPDOWN = By.xpath("//div[@id='plans']");
    private static final By NA_PLAN_LINK = By.xpath("//li[@data-value='NOT_EXISTS']");
    private static final By NO_ITEM_AVAILABLE_TEXT = By.xpath("//div[text()='No results']");
    private static final By SELECT_DEVICES_CHECKBOX = By.xpath("//input[contains(@aria-label,'Select all')]");
    private static final By DELETE_DEVICES_BUTTON = By.xpath("//button[@id='remove-device']");
    private static final By BULK_UPLOAD_LINK = By.xpath("//p[text()='Bulk unenroll devices from an imported .CSV file']");
    private static final By SELECTED_DEVICES_LINK = By.xpath("//p[text()='Delete selected devices from list']");
    private static final By SECURITY_CODE = By.xpath("//h6[@id='security-code-number']/child::strong");
    private static final By SECURITY_CODE_TEXTBOX = By.xpath("//input[@id='security-code']");
    private static final By DELETE_FILE_INPUT_ON_DELETE_DEVICES_SCREEN = By.xpath("//input[@id='browse-file']");
    private static final By DELETE_BUTTON_ON_DELETE_DEVICES_SCREEN = By.xpath("//button[@id='delete-device']");
    private static final By NO_RESULTS_TEXT = By.xpath("//div[text()='No results']");


    public static PCPage_WXP getInstance() {
        return PC_PAGE_WXP_INSTANCE;
    }

    private static void clearFiltersIfApplied() {
        waitForPeriodOf(5);
        if (isElementDisplayed(CLEAR_FILTERS_LINK)) {
            clickByJS(CLEAR_FILTERS_LINK);
        }
    }

    public PCPage_WXP assertDevice_IsDisplayed_To_EUTenant() {
        this.serialNumber = SerialNumberHolder.getSerialNumber();

        clearFiltersIfApplied();
        waitForPeriodOf(5);
        sendKeys(SERIAL_NUMBER_FILTER, serialNumber);

        xpathForSerialNumberLink = getDynamicXpath(SERIAL_NUMBER_LINK, serialNumber);

        assertThat((isDisplayed(xpathForSerialNumberLink)))
                .withFailMessage("Mapped Device is not shown for EU tenant")
                .isTrue();

        LOGGER.info("Mapped Device is shown for EU tenant");
        return this;
    }

    public PCDetailPage_WXP assertDeviceStatusIs(DeviceStatus expectedStatus) {
        By xpathForSerialNumberStatus = getDynamicXpath(SERIAL_NUMBER_STATUS, serialNumber);
        String actualStatus = waitTillDisplay(xpathForSerialNumberStatus).getText();

        assertThat(actualStatus)
                .withFailMessage("Expected status: '%s', but found: '%s'", expectedStatus.getValue(), actualStatus)
                .isEqualToIgnoringCase(expectedStatus.getValue());

        LOGGER.info("Device Status is : {}", expectedStatus);

        click(xpathForSerialNumberLink);
        return PCDetailPage_WXP.getInstance();
    }

    public PCPage_WXP assertCountOfDevices(int expectedCount) {
        clearFiltersIfApplied();

        final int[] actualCount = {0};
        boolean isDeviceCountAsExpected = waitTillConditionWithRefresh(() -> {
            String tempText = scrollAndFindElement(DEVICE_COUNT).getText();
            actualCount[0] = Integer.parseInt(tempText.replaceAll("[^0-9]", ""));
            return actualCount[0] == expectedCount;
        }, 120);

        assertThat(isDeviceCountAsExpected)
                .withFailMessage(String.format("Device count mismatch :: Expected: %d, Actual: %d", expectedCount, actualCount[0]))
                .isTrue();

        LOGGER.info("All {} devices are mapped to wxp tenant as expected", actualCount[0]);
        return this;
    }

    public void assertAllDevicesEnrolledAre_AssociatedWith_ActiveCarePlan() {
        LOGGER.info("Waiting for all devices to be associated with HP Active Care Plan...");

        boolean isConfirmationMessageDisplayed = waitTillConditionWithRefresh(() -> {
            clearFiltersIfApplied();
            selectPageSizeAs100();
            click(PLANS_DROPDOWN);
            click(NA_PLAN_LINK);

            return waitTillCondition(() -> isDisplayed(NO_ITEM_AVAILABLE_TEXT), 15);
        }, 300);

        softly().assertThat(isConfirmationMessageDisplayed)
                .withFailMessage("Devices are having N/A plan even after maximum wait time")
                .isTrue();

        int countOfDevicesWith_NA_Plan = fetchCountOfDevicesWith_NAPlan_ForWxpTenant();

        softly().assertThat(countOfDevicesWith_NA_Plan)
                .withFailMessage("%d of devices are assigned N/A plan", countOfDevicesWith_NA_Plan)
                .isZero();

        assertAll();
        LOGGER.info("All devices transferred are associated with HP Active Care Plan");
        if(isRunningTestPerfCUJ()) {
            endTimer("ac_plan_allocation_in_wxp");
        }
    }

    private static void selectPageSizeAs100() {
        click(PAGE_SIZE_BUTTON);
        click(PAGE_SIZE_100);
    }

    public PCPage_WXP deleteDevicesAsWxpTenant() {
        clearFiltersIfApplied();
        waitForPeriodOf(5);

        clickByJS(SELECT_DEVICES_CHECKBOX);
        waitForPeriodOf(5);
        clickByJS(DELETE_DEVICES_BUTTON);
        click(BULK_UPLOAD_LINK);
        sendKeys(SECURITY_CODE_TEXTBOX, getText(SECURITY_CODE));
        sendKeys(DELETE_FILE_INPUT_ON_DELETE_DEVICES_SCREEN, getCsvForDeviceUnroll());
        click(DELETE_BUTTON_ON_DELETE_DEVICES_SCREEN);
        if(isRunningTestPerfCUJ()) {
            startTimer("delete_devices_as_wxp_tenant");
        }
        return this;
    }

    public void assertThatWxpTenantDeletedDevicesSuccessfully() {
        clearFiltersIfApplied();

        boolean isNoResultsTextDisplayed = waitTillConditionWithRefresh(() ->
                isDisplayed(NO_RESULTS_TEXT),240
        );

        assertThat(isNoResultsTextDisplayed)
                .withFailMessage("Not all devices deleted after wxp tenant delete operation")
                .isTrue();

        LOGGER.info("As wxp tenant, deleted all devices");
        if(isRunningTestPerfCUJ()) {
            endTimer("delete_devices_as_wxp_tenant");
        }
    }

    public HomePage_WXP deleteExistingDevicesBeforeTest() {
        clearFiltersIfApplied();

        if(isDisplayed(NO_RESULTS_TEXT)) {
            LOGGER.info("No devices exist for wxp tenant to delete");
        }
        else {
            deleteExistingDevices();
        }
        return HomePage_WXP.getInstance();
    }

    private void deleteExistingDevices() {

        clearFiltersIfApplied();
        selectPageSizeAs100();
        waitForPeriodOf(5);

        while(!isDisplayed(NO_RESULTS_TEXT)) {
            clickByJS(SELECT_DEVICES_CHECKBOX);
            waitForPeriodOf(5);
            clickByJS(DELETE_DEVICES_BUTTON);
            click(SELECTED_DEVICES_LINK);
            sendKeys(SECURITY_CODE_TEXTBOX, getText(SECURITY_CODE));
            click(DELETE_BUTTON_ON_DELETE_DEVICES_SCREEN);
            PreDefinedActions.getDriver().navigate().refresh();
            waitTillDocumentReady();
            clearFiltersIfApplied();
        }
        LOGGER.info("existing devices cleaned up before test");
    }
}
