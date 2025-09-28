package pages;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.OrderType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import java.util.Map;

import static com.e2e_activecare.utils.BusinessLogicUtil.getExpectedDeviceStatus;
import static com.e2e_activecare.utils.BusinessLogicUtil.getOrderTypeToPlanNameMap;
import static com.e2e_activecare.utils.UIUtil.*;
import static com.e2e_activecare.utils.WaitUtil.waitTillDisplay;
import static org.assertj.core.api.Assertions.assertThat;

public final class DeviceDetailsPage {

    private DeviceDetailsPage() {}

    private static final DeviceDetailsPage DEVICE_DETAILS_PAGE_INSTANCE = new DeviceDetailsPage();
    private static final Logger LOGGER = LogManager.getLogger(DeviceDetailsPage.class);

    private static final By ACTIVE_CARE_TEXT = By.xpath("//p[text()='HP Active Care']");
    private static final By REMOTE_CARE_TEXT = By.xpath("//p[text()='HP Remotecare']");
    private static final By ENROLLMENT_STATUS = By.xpath("//div[@class='dui-icon-label']");
    public static final By ACTIVE_CARE_ENROLLMENT_START_DATE = By.xpath("//span[text()='HP Active Care']/following-sibling::p[contains(text(), 'Enrolled on')]");
    public static final By ACTIVE_CARE_ENROLLMENT_END_DATE = By.xpath("//span[text()='HP Active Care']/following-sibling::p[contains(text(), 'Expires on')]");
    public static final By REMOTE_CARE_ENROLLMENT_START_DATE = By.xpath("//span[text()='HP Remotecare']/following-sibling::p[contains(text(), 'Enrolled on')]");
    public static final By REMOTE_CARE_ENROLLMENT_END_DATE = By.xpath("//span[text()='HP Remotecare']/following-sibling::p[contains(text(), 'Expires on')]");


    public static DeviceDetailsPage getInstance() {
        return DEVICE_DETAILS_PAGE_INSTANCE;
    }

    public DeviceDetailsPage assertRegisteredDevice_Status() {
        String actualStatus = waitTillDisplay(ENROLLMENT_STATUS).getText();
        String expectedStatus = getExpectedDeviceStatus().getValue();

        assertThat(actualStatus)
                .withFailMessage(String.format("Expected device status : %s, Actual device status : %s", expectedStatus, actualStatus))
                .isEqualToIgnoringCase(expectedStatus);

        LOGGER.info("Device status is {}", expectedStatus);
        return this;
    }

    public void assertDeviceIsAssociatedWith_RightPlan() {
        OrderType[] orderTypes = TestContextHolder.getCurrentOrderTypes();

        Map<OrderType, By> orderTypeToLocatorMap = Map.of(
                OrderType.ACTIVE_CARE, ACTIVE_CARE_TEXT,
                OrderType.PREMIUM, ACTIVE_CARE_TEXT,
                OrderType.PREMIUM_PLUS, ACTIVE_CARE_TEXT,
                OrderType.OUT_OF_BAND_REMEDIATION, REMOTE_CARE_TEXT
        );

        Map<OrderType, String> orderTypeToPlanNameMap = getOrderTypeToPlanNameMap();

        for (OrderType orderType : orderTypes) {
            By locator = orderTypeToLocatorMap.get(orderType);
            String expectedPlanName = orderTypeToPlanNameMap.get(orderType);

            if (locator == null || expectedPlanName == null) {
                throw new IllegalArgumentException("Unknown order type: " + orderType);
            }

            String actualPlanName = waitTillDisplay(locator).getText();
            assertThat(actualPlanName)
                    .withFailMessage("Device is not associated with the expected plan. Expected: %s, Actual: %s", expectedPlanName, actualPlanName)
                    .isEqualTo(expectedPlanName);

            LOGGER.info("Device is associated with the correct plan: {}", expectedPlanName);

            fetchingDeviceIdFromURL();
            fetchPlanEnrollmentStartEndDates(orderType);
        }
    }

    public DevicesPage navigateBackToDevicePage() {
        PreDefinedActions.getDriver().navigate().back();
        return DevicesPage.getInstance();
    }
}
