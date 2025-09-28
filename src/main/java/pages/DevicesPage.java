package pages;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.enums.OrderType;
import com.e2e_activecare.pojo.PartnerDeviceTransfer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Map;

import static com.e2e_activecare.constants.FrameWorkConstants.getCsvToAddDevicesAsMSP;
import static com.e2e_activecare.constants.FrameWorkConstants.getCsvToDeleteDevicesAsMSP;
import static com.e2e_activecare.context.TestContextHolder.getCurrentOrderTypes;
import static com.e2e_activecare.enums.OrderType.PARTNER_DEVICE_TRANSFER_ACPI_TO_AC;
import static com.e2e_activecare.enums.OrderType.PARTNER_DEVICE_TRANSFER_AC_TO_ACPI;
import static com.e2e_activecare.pages.HomePage.assertPartnerDeviceTransferIsSuccessful;
import static com.e2e_activecare.pojo.context.SerialNumberHolder.getSerialNumber;
import static com.e2e_activecare.utils.BusinessLogicUtil.*;
import static com.e2e_activecare.utils.PerformanceMetricUtil.endTimer;
import static com.e2e_activecare.utils.PerformanceMetricUtil.startTimer;
import static com.e2e_activecare.utils.APIUtil.fetchCountOfDevicesWith_NAPlan;
import static com.e2e_activecare.utils.AssertUtil.assertAll;
import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.DynamicXpathUtil.getDynamicXpath;
import static com.e2e_activecare.utils.UIUtil.*;
import static com.e2e_activecare.utils.WaitUtil.*;
import static com.e2e_activecare.enums.AttributeType.TITLE;
import static org.assertj.core.api.Assertions.assertThat;

public final class DevicesPage {

    private DevicesPage() {}

    private static final DevicesPage DEVICES_PAGE_INSTANCE = new DevicesPage();
    private static final Logger LOGGER = LogManager.getLogger(DevicesPage.class);

    private static final By PENDING_ENROLLMENT_LINK = By.xpath("//span[text()='Pending Enrollment']");
    private static final By ALL_DEVICES_LINK = By.xpath("//span[text()='All']");
    private static final By CLEAR_FILTERS_LINK = By.xpath("//button[@aria-label='Clear Filters']");
    private static final By SERIAL_NUMBER_FILTER = By.xpath("//input[@id='serialNumber']");
    private static final String SERIAL_NUMBER_LINK = "//a[text()='%s']";
    private static final By DEVICE_SELECT_CHECKBOX = By.xpath("//input[@aria-label='Select all 1 items']");
    private static final By SECURITY_CODE = By.xpath("//h4[@class='noselect']");
    private static final By SECURITY_CODE_TEXTBOX = By.xpath("//input[@id='security-code']");
    private static final By DELETE_CONFIRMATION_TEXT = By.xpath("//span[text()='No items available']");
    private static final By DELETE_BUTTON_MSP = By.xpath("//span[text()='Delete']/parent::button[@id='remove-device']");
    private static final By DELETE_DEVICES_CONTINUE_BUTTON = By.xpath("(//div[@class='dui-wizard-modal-buttons dui-wizard-modal-buttons-right'])[2]");
    private static final By DELETE_BUTTON_ON_DELETE_DEVICES_SCREEN = By.xpath("//span[text()='Delete']/parent::button[@class='dui-wizard-modal-buttons-next css-8clz94']");
    private static final By DELETE_FILE_INPUT_ON_DELETE_DEVICES_SCREEN = By.xpath("(//input[@id='browse-file'])[2]");

    private static final By COMPANY_NAME_FILTER = By.xpath("//div[@id='companyName']");
    private static final String SOURCE_COMPANY_NAME = "//span[text()='%s']/parent::div";
    private static final By FIRST_SERIAL_NUMBER = By.xpath("//table[@id='devices_pending']/tbody/tr[1]/td[2]/a");
    private static final By ALL_LINK = By.xpath("//span[text()='All']");
    private static final By TRANSFER_BUTTON = By.xpath("//div[contains(@class, 'dui-table-horizontal-group')]//button[@id='transfer-btn']");
    private static final By UPLOAD_FILE = By.cssSelector("input[type='file']");
    private static final By IMPORT_BUTTON = By.xpath("//button[@id='import-order']");
    private static final String COMPANY_SERIAL_NUMBER_MAPPED_TO = "//a[text()='%s']/ancestor::tr/td[4]/a";
    private static final By COMPANY_NAME_DROPDOWN = By.xpath("//div[@aria-labelledby='companyName']");
    private static final By COMPANY_NAME_SEARCH_TEXTBOX = By.xpath("//input[@placeholder='Search' and @Class='css-1hqc758']");
    private static final String COMPANY_NAME_SEARCH_RESULT = "//span[text()='%s' and not(@class)]";

    private static final By DEVICE_COUNT = By.xpath("//button[@aria-label='Select page size']/following-sibling::span[starts-with(text(), 'of')]");
    private static final By PLANS_SELECTION = By.xpath("//input[@name='plans']/following-sibling::span/div[@aria-label='Open']");
    private static final By NA_PLAN_LINK = By.xpath("//ul[@id='plans-listbox']/li[@data-value='NOT_EXISTS']");
    private static final By AC_PLAN_LINK = By.xpath("//ul[@id='plans-listbox']/li[@data-value='plans.package_name.displayName.hp_care_pack_2']");
    private static final By RC_PLAN_LINK = By.xpath("//ul[@id='plans-listbox']/li[@data-value='plans.package_name.displayName.hp_remotecare']");
    private static final By NO_ITEM_AVAILABLE_TEXT = By.xpath("//span[text()='No items available']");
    private static final By PAGE_SIZE_BUTTON = By.xpath("//button[@aria-label='Select page size']");
    private static final By PAGE_SIZE_100 = By.xpath("//span[text()='100']/parent::li");

    private static final By ADD_DEVICE_BUTTON = By.xpath("(//span[text()='Add']/parent::button[@id='add-device'])[2]");
    private static final By AUTO_ENROLLMENT_LINK = By.xpath("//p[text()='Devices automatically enroll when they boot up.']");
    private static final By NEXT_BUTTON = By.xpath("//span[text()='Next']/parent::button");
    private static final By UPLOAD_FILE_LINK = By.xpath("//p[text()='Import multiple serial numbers from a CSV file.']");
    private static final By ADD_DEVICES_INPUT_ON_ADD_DEVICES_SCREEN = By.xpath("(//input[@id='browse-file'])[1]");
    private static final By IMPORT_BUTTON_ON_ADD_DEVICES_SCREEN = By.xpath("(//span[text()='Import']/parent::button)[1]");

    private static final By SELECT_DEVICE_CHECKBOX = By.xpath("//input[@type='checkbox' and contains(@aria-label,'Select all')]");
    private static final By SELECTED_PLANS = By.xpath("//span[text()='Selected']/../li[@aria-selected='true']");


    public static DevicesPage getInstance() {
        return DEVICES_PAGE_INSTANCE;
    }

    public DevicesPage clickPendingEnrollmentTab() {
        waitForPeriodOf(5);
        click(PENDING_ENROLLMENT_LINK);
        return this;
    }

    public DevicesPage navigateToAllDevicesSection() {
        click(ALL_DEVICES_LINK);
        return DevicesPage.getInstance();
    }

    private static void clearFiltersIfApplied() {
        waitForPeriodOf(5);
        if (isElementDisplayed(CLEAR_FILTERS_LINK)) {
            click(CLEAR_FILTERS_LINK);
            waitForPeriodOf(5);
        }
    }

    public DeviceDetailsPage assertRegisteredDevice_IsDisplayed_To_ACHTTenant() {
        clearFiltersIfApplied();
        String serialNumber = getSerialNumber();

        sendKeys(SERIAL_NUMBER_FILTER,serialNumber);
        By xpathForSerialNumberLink = getDynamicXpath(SERIAL_NUMBER_LINK, serialNumber);

        assertThat((isDisplayed(xpathForSerialNumberLink)))
                  .withFailMessage("Registered Device is not shown for ACHT tenant")
                  .isTrue();

        LOGGER.info("Registered Device is shown for ACHT tenant");

        click(xpathForSerialNumberLink);
        return DeviceDetailsPage.getInstance();
    }

    public DeviceDetailsPage assertRegisteredDevice_IsDisplayed_To_DestinationTenant() {
        clearFiltersIfApplied();
        String serialNumber = getSerialNumber();

        sendKeys(SERIAL_NUMBER_FILTER,serialNumber);
        By xpathForSerialNumberLink = getDynamicXpath(SERIAL_NUMBER_LINK, serialNumber);

        assertThat((isDisplayed(xpathForSerialNumberLink)))
                .withFailMessage("Registered Device is not shown for Destination tenant")
                .isTrue();

        click(xpathForSerialNumberLink);
        LOGGER.info("Registered Device is shown for Destination tenant");
        return DeviceDetailsPage.getInstance();
    }

    public DevicesPage assertCountOfDevices(int expectedCount) {
        clearFiltersIfApplied();

        final int[] actualCount = {0};
        boolean isDeviceCountAsExpected = waitTillConditionWithRefresh(() -> {
            String tempText = scrollAndFindElement(DEVICE_COUNT).getText();
            actualCount[0] = Integer.parseInt(tempText.replaceAll("[^0-9]", ""));
            return actualCount[0] == expectedCount;
        },120);

        assertThat(isDeviceCountAsExpected)
                .withFailMessage(String.format("Device count mismatch :: Expected: %d, Actual: %d", expectedCount, actualCount[0]))
                .isTrue();

        LOGGER.info("All {} devices are transferred to destination tenant as expected", actualCount[0]);
        return this;
    }

    public void assertAllDevicesTransferred_AssociatedWith_RightPlan() {
        LOGGER.info("Waiting for all devices to be associated with HP Active Care Plan...");

        validateNoDevicesAssignedNAPlan();
        validateAllDevicesAssignedWithRightPlan();

        assertAll();
        LOGGER.info("All devices transferred are associated with right plan}");
        if(isRunningTestPerfCUJ()){
            endTimer("ac_plan_allocation");
        }
    }

    private static void validateAllDevicesAssignedWithRightPlan() {

        OrderType[] orderTypes = getCurrentOrderTypes();
        Map<OrderType, String> orderTypePlanNameMap = getOrderTypeToPlanNameMap();
        Map<String, By> planNameToLink = getPlanNameToLinkMap();
        int expectedDeviceCount = getNumberOfDevices();

        for (OrderType orderType : orderTypes) {
            String expectedPlanName = orderTypePlanNameMap.get(orderType);

            clearFiltersIfApplied();
            selectPageSizeAs100();
            click(PLANS_SELECTION);
            clearSelectedPlans();
            By planLink = planNameToLink.get(expectedPlanName);
            click(planLink);

            final int[] actualCount = {0};
            boolean isDeviceCountAsExpected = waitTillConditionWithRefresh(() -> {
                String tempText = scrollAndFindElement(DEVICE_COUNT).getText();
                actualCount[0] = Integer.parseInt(tempText.replaceAll("[^0-9]", ""));
                return actualCount[0] == expectedDeviceCount;
            },120);

            softly().assertThat(isDeviceCountAsExpected)
                    .withFailMessage(String.format("Device count with %s mismatch :: Expected: %d, Actual: %d", expectedPlanName, expectedDeviceCount, actualCount[0]))
                    .isTrue();
        }
    }

    private static void clearSelectedPlans() {
        for (WebElement element : PreDefinedActions.getDriver().findElements(SELECTED_PLANS)) {
            element.click();
        }
    }

    private static Map<String, By> getPlanNameToLinkMap() {
        return Map.of(
                "HP Active Care", AC_PLAN_LINK,
                "HP Remotecare", RC_PLAN_LINK
        );
    }

    private static void validateNoDevicesAssignedNAPlan() {
        boolean isConfirmationMessageDisplayed = waitTillConditionWithRefresh(() -> {
            clearFiltersIfApplied();
            waitForPeriodOf(5);
            selectPageSizeAs100();
            click(PLANS_SELECTION);
            click(NA_PLAN_LINK);

            return waitTillCondition(() -> isDisplayed(NO_ITEM_AVAILABLE_TEXT), 15);
        }, 120);

        softly().assertThat(isConfirmationMessageDisplayed)
                .withFailMessage("Devices are having N/A plan even after maximum wait time")
                .isTrue();

        int countOfDevicesWith_NA_Plan = fetchCountOfDevicesWith_NAPlan();
        softly().assertThat(countOfDevicesWith_NA_Plan)
                .withFailMessage("%d devices are still assigned N/A plan", countOfDevicesWith_NA_Plan)
                .isZero();
    }

    private static void selectPageSizeAs100() {
        clickByJS(PAGE_SIZE_BUTTON);
        scrollAndClick(PAGE_SIZE_100);
    }

    public HomePage deleteDeviceForDestinationTenant() {
        clearFiltersIfApplied();
        String serialNumber = getSerialNumber();
        sendKeys(SERIAL_NUMBER_FILTER,serialNumber);

        By xpathForSerialNumberLink = getDynamicXpath(SERIAL_NUMBER_LINK, serialNumber);

        assertThat((isDisplayed(xpathForSerialNumberLink)))
                .withFailMessage("Registered Device is not shown for Destination tenant")
                .isTrue();

        clickByJS(DEVICE_SELECT_CHECKBOX);
        click(DELETE_BUTTON_MSP);
        click(DELETE_DEVICES_CONTINUE_BUTTON);
        sendKeys(SECURITY_CODE_TEXTBOX, getText(SECURITY_CODE));
        click(DELETE_BUTTON_ON_DELETE_DEVICES_SCREEN);

        assertThat(waitTillDisplay(DELETE_CONFIRMATION_TEXT).isDisplayed())
                .withFailMessage("Device delete confirmation message is not displayed")
                .isTrue();

        LOGGER.info("Device with serial number {} deleted successfully",serialNumber);
        return HomePage.getInstance();
    }

    public DevicesPage deleteDevicesAsMSP() {
        clearFiltersIfApplied();
        selectCompanyName();

        boolean isDeleteButtonDisplayed = waitTillConditionWithRefresh(() ->
                waitTillDisplay(DELETE_BUTTON_MSP).isDisplayed(),60
        );

        assertThat(isDeleteButtonDisplayed)
                .withFailMessage("No devices exist for MSP to delete")
                .isTrue();

        deleteDevices();
        return this;
    }

    public HomePage deleteExistingDevicesBeforeTest() {
        clearFiltersIfApplied();
        selectCompanyName();

        boolean isDeleteButtonDisplayed = waitTillConditionWithRefresh(() ->
                waitTillDisplay(DELETE_BUTTON_MSP).isDisplayed(),30
        );

        if(isDeleteButtonDisplayed) {
            deleteExistingDevices();
        }
        else {
            LOGGER.info("No devices exist for MSP to delete");
        }
        return HomePage.getInstance();
    }

    public HomePage assertThatMSPDeletedDevicesSuccessfully() {

        clearFiltersIfApplied();
        selectCompanyName();

        boolean isDeleteButtonGone = waitTillConditionWithRefresh(() ->
                waitTillInvisibilityOfElement(DELETE_BUTTON_MSP),1200
        );

        assertThat(isDeleteButtonGone)
                .withFailMessage("Not all devices deleted after MSP delete operation")
                .isTrue();

        LOGGER.info("As MSP, deleted all devices associated with tenant");
        if(isRunningTestPerfCUJ()){
            endTimer("delete_devices_as_msp");
        }
        return HomePage.getInstance();
    }

    private static void deleteDevices() {
        click(DELETE_BUTTON_MSP);
        click(DELETE_DEVICES_CONTINUE_BUTTON);
        sendKeys(SECURITY_CODE_TEXTBOX, getText(SECURITY_CODE));

        sendKeysForMSP(DELETE_FILE_INPUT_ON_DELETE_DEVICES_SCREEN, getCsvToDeleteDevicesAsMSP());
        click(DELETE_BUTTON_ON_DELETE_DEVICES_SCREEN);
        if(isRunningTestPerfCUJ()){
            startTimer("delete_devices_as_msp");
        }
    }

    private void deleteExistingDevices() {

        clearFiltersIfApplied();
        selectCompanyName();
        selectPageSizeAs100();
        waitForPeriodOf(5);

        while(isDisplayed(DELETE_BUTTON_MSP)) {
            clickByJS(SELECT_DEVICE_CHECKBOX);
            click(DELETE_BUTTON_MSP);
            click(DELETE_DEVICES_CONTINUE_BUTTON);
            sendKeys(SECURITY_CODE_TEXTBOX, getText(SECURITY_CODE));
            click(DELETE_BUTTON_ON_DELETE_DEVICES_SCREEN);
            PreDefinedActions.getDriver().navigate().refresh();
            clearFiltersIfApplied();
            selectCompanyName();
            waitForPeriodOf(5);
        }
        LOGGER.info("existing devices cleaned up before test");
    }

    private static void selectCompanyName() {
        click(COMPANY_NAME_DROPDOWN);
        sendKeys(COMPANY_NAME_SEARCH_TEXTBOX, getCompanyName());
        click(getDynamicXpath(COMPANY_NAME_SEARCH_RESULT, getCompanyName()));
    }

    public DevicesPage performDeviceTransferFromAcCustomerToAcpiCustomer() {
        clearFiltersIfApplied();
        pickSerialNumberToTransferFromAcToAcpiCustomer();
        verifyToWhichCompanySerialNumberIsMapped_BeforeTransfer();
        uploadCSVToInitiatePartnerDeviceTransferFromAcToAcpiCustomer();
        //assertPartnerDeviceTransferIsSuccessful();
        return this;
    }

    public DevicesPage performDeviceTransferFromAcpiCustomerToAcCustomer() {
        clearFiltersIfApplied();
        pickSerialNumberToTransferFromAcpiToAcCustomer();
        verifyToWhichCompanySerialNumberIsMapped_BeforeTransfer();
        uploadCSVToInitiatePartnerDeviceTransferFromAcpiToAcCustomer();
        //assertPartnerDeviceTransferIsSuccessful();
        return this;
    }

    public HomePage assertDeviceIsTransferredToAcpiCustomer() {
        clickPendingEnrollmentTab();
        waitForPeriodOf(2);

        String serialNumber = getSerialNumber();
        By xpathForCompany = getDynamicXpath(COMPANY_SERIAL_NUMBER_MAPPED_TO, serialNumber);
        String expectedCompanyName = TestConfig.get("acpiCustomerCompanyName");

        boolean isTransferred = waitTillConditionWithRefresh(() -> {
            clearFiltersIfApplied();
            sendKeys(SERIAL_NUMBER_FILTER, serialNumber);
            String currentCompanyName = getText(xpathForCompany);
            return expectedCompanyName.equals(currentCompanyName);
        }, 120);

        assertThat(isTransferred)
                .withFailMessage(String.format("Serial Number %s is not transferred to %s within maximum wait time set", serialNumber, expectedCompanyName))
                .isTrue();

        LOGGER.info("Serial Number {} is transferred to {} tenant", serialNumber, expectedCompanyName);
        return HomePage.getInstance();
    }

    public HomePage assertDeviceIsTransferredToAcCustomer() {
        clickPendingEnrollmentTab();
        waitForPeriodOf(2);

        String serialNumber = getSerialNumber();
        By xpathForCompany = getDynamicXpath(COMPANY_SERIAL_NUMBER_MAPPED_TO, serialNumber);
        String expectedCompanyName = TestConfig.get("acCustomerCompanyName");

        boolean isTransferred = waitTillConditionWithRefresh(() -> {
            clearFiltersIfApplied();
            sendKeys(SERIAL_NUMBER_FILTER, serialNumber);
            String currentCompanyName = getText(xpathForCompany);
            return expectedCompanyName.equals(currentCompanyName);
        }, 120);

        assertThat(isTransferred)
                .withFailMessage(String.format("Serial Number %s is not transferred to %s within maximum wait time set", serialNumber, expectedCompanyName))
                .isTrue();

        LOGGER.info("Serial Number {} is transferred to {} tenant", serialNumber, expectedCompanyName);
        return HomePage.getInstance();
    }

    public void assertDeviceIsDisplayedToAcpiCustomer() {
        clearFiltersIfApplied();

        String serialNumber = getSerialNumber();
        sendKeys(SERIAL_NUMBER_FILTER,serialNumber);
        By xpathForSerialNumberLink = getDynamicXpath(SERIAL_NUMBER_LINK, serialNumber);

        assertThat((isDisplayed(xpathForSerialNumberLink)))
                .withFailMessage("Transferred Device is not shown to acpi customer")
                .isTrue();

        click(xpathForSerialNumberLink);
        LOGGER.info("Transferred Device is shown to acpi customer");
    }

    public void assertDeviceIsDisplayedToAcCustomer() {
        clearFiltersIfApplied();

        String serialNumber = getSerialNumber();
        sendKeys(SERIAL_NUMBER_FILTER,serialNumber);
        By xpathForSerialNumberLink = getDynamicXpath(SERIAL_NUMBER_LINK, serialNumber);

        assertThat((isDisplayed(xpathForSerialNumberLink)))
                .withFailMessage("Transferred Device is not shown to ac customer")
                .isTrue();

        click(xpathForSerialNumberLink);
        LOGGER.info("Transferred Device is shown to ac customer");
    }

    private static void verifyToWhichCompanySerialNumberIsMapped_BeforeTransfer() {
        String serialNumber = getSerialNumber();
        By xpathForCompany = getDynamicXpath(COMPANY_SERIAL_NUMBER_MAPPED_TO, serialNumber);
        LOGGER.info("Serial Number {} is mapped to {} tenant currently",serialNumber, getText(xpathForCompany));
    }

    private static void uploadCSVToInitiatePartnerDeviceTransferFromAcToAcpiCustomer() {
        click(ALL_LINK);
        click(TRANSFER_BUTTON);

        waitAndUploadCSVFileThroughSendKeys(UPLOAD_FILE, PARTNER_DEVICE_TRANSFER_AC_TO_ACPI, PartnerDeviceTransfer.class);
        click(IMPORT_BUTTON);
        LOGGER.info("CSV to process partner device transfer is uploaded successfully");
    }

    private static void uploadCSVToInitiatePartnerDeviceTransferFromAcpiToAcCustomer() {
        click(ALL_LINK);
        click(TRANSFER_BUTTON);

        waitAndUploadCSVFileThroughSendKeys(UPLOAD_FILE, PARTNER_DEVICE_TRANSFER_ACPI_TO_AC, PartnerDeviceTransfer.class);
        click(IMPORT_BUTTON);
        LOGGER.info("CSV to process partner device transfer is uploaded successfully");
    }

    private static void pickSerialNumberToTransferFromAcToAcpiCustomer() {
        waitForPeriodOf(2);
        click(COMPANY_NAME_FILTER);        ;
        click(getDynamicXpath(SOURCE_COMPANY_NAME, TestConfig.get("acCustomerCompanyName")));

        String serialNumber = waitAndGetAttribute(FIRST_SERIAL_NUMBER, TITLE.getValue());
        com.e2e_activecare.pojo.context.SerialNumberHolder.setSerialNumber(serialNumber);
        LOGGER.info("Serial Number {} picked to transfer",serialNumber);
    }

    private static void pickSerialNumberToTransferFromAcpiToAcCustomer() {
        waitForPeriodOf(2);
        click(COMPANY_NAME_FILTER);        ;
        click(getDynamicXpath(SOURCE_COMPANY_NAME, TestConfig.get("acpiCustomerCompanyName")));

        String serialNumber = waitAndGetAttribute(FIRST_SERIAL_NUMBER, TITLE.getValue());
        com.e2e_activecare.pojo.context.SerialNumberHolder.setSerialNumber(serialNumber);
        LOGGER.info("Serial Number {} picked to transfer",serialNumber);
    }

    public DevicesPage performAssetUpload() {
        click(ADD_DEVICE_BUTTON);
        click(AUTO_ENROLLMENT_LINK);
        click(NEXT_BUTTON);
        click(UPLOAD_FILE_LINK);
        click(NEXT_BUTTON);
        sendKeysForMSP(ADD_DEVICES_INPUT_ON_ADD_DEVICES_SCREEN, getCsvToAddDevicesAsMSP());
        clickByJS(IMPORT_BUTTON_ON_ADD_DEVICES_SCREEN);

        return this;
    }

    public HomePage assertCountOfDevicesAddedByMSP(int expectedCount) {
        clearFiltersIfApplied();
        selectCompanyName();

        final int[] actualCount = {0};
        boolean isDeviceCountAsExpected = waitTillConditionWithRefresh(() -> {
            String tempText = scrollAndFindElement(DEVICE_COUNT).getText();
            actualCount[0] = Integer.parseInt(tempText.replaceAll("[^0-9]", ""));
            return actualCount[0] == expectedCount;
        },120);

        assertThat(isDeviceCountAsExpected)
                .withFailMessage(String.format("Device count mismatch :: Expected: %d, Actual: %d", expectedCount, actualCount[0]))
                .isTrue();

        LOGGER.info("All {} devices added by MSP are shown under devices section of MSP login", actualCount[0]);
        return HomePage.getInstance();
    }
}
