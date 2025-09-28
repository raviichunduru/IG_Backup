package pages.wxp;

import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.Environment;
import com.e2e_activecare.enums.OrderType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import static com.e2e_activecare.context.EnvironmentHolder.getEnvironment;
import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.UIUtil.*;
import static com.e2e_activecare.utils.WaitUtil.*;

public final class PCDetailPage_WXP {

    private PCDetailPage_WXP () {}

    private static final PCDetailPage_WXP PC_DETAIL_PAGE_WXP_INSTANCE = new PCDetailPage_WXP();
    private static final Logger LOGGER = LogManager.getLogger(PCDetailPage_WXP.class);

    private static final By ACTIVE_CARE_PLAN_STABLE = By.xpath("//span[@id='warranty-service-coverage-planname-0'][text()='HP Active Care']");
    private static final By ACTIVE_CARE_PLAN_STAGING = By.xpath("//span[@id='enrollment-plan-name-0'][text()='HP Active Care']");

    public static PCDetailPage_WXP getInstance() {
        return PC_DETAIL_PAGE_WXP_INSTANCE;
    }

    public void assertDevicePlans() {
        waitTillDocumentReady();
        waitForPeriodOf(5);

        OrderType[] orderTypes = TestContextHolder.getCurrentOrderTypes();

        for (OrderType orderType : orderTypes) {
            if (orderType == OrderType.OUT_OF_BAND_REMEDIATION) {
                LOGGER.info("No UI plan validation for order type OOBR, as AC won't be assigned");
            } else {
                By acPlanLocator = getACPlanNameLocatorBasedOnEnv();
                boolean isACPlanAssigned = scrollAndFindElement(acPlanLocator).isDisplayed();
                softly()
                        .assertThat(isACPlanAssigned)
                        .withFailMessage("Active Care Plan is not assigned to device for order type: %s", orderType)
                        .isTrue();
            }
        }
    }

    private By getACPlanNameLocatorBasedOnEnv() {
        Environment environment = getEnvironment();
        switch (environment) {
            case US_STABLE:
            case EU_STABLE:
                return ACTIVE_CARE_PLAN_STABLE;
            case US_STAGING:
            case EU_STAGING:
                return ACTIVE_CARE_PLAN_STAGING;
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }
}
