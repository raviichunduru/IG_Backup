package pages.wxp;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.pojo.context.SerialNumberHolder;
import com.e2e_activecare.pages.IncidentDetailsPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.UIUtil.scrollAndFindElement;
import static com.e2e_activecare.utils.WaitUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public final class HardwareSupportPage_WXP {

    private HardwareSupportPage_WXP () {}

    private static final HardwareSupportPage_WXP HARDWARE_SUPPORT_PAGE_WXP = new HardwareSupportPage_WXP();
    private static final Logger LOGGER = LogManager.getLogger(HardwareSupportPage_WXP.class);

    private static final By CREATE_CASE_BUTTON = By.xpath("//div[@class='dui-table-horizontal-group dui-one-line']/button[@id='create-case-button']");
    private static final By SUCCESS_NOTIFICATION = By.xpath("//div[text()='Case created successfully.']");
    private static final By DEVICE_SERIAL_NUMBER_FILTER = By.xpath("//input[@name='deviceSerial']");
    private static final By CASE_COUNT = By.xpath("//button[@aria-label='Select page size']/following-sibling::span[starts-with(text(), 'of')]");
    private static final By CLEAR_FILTERS_LINK = By.xpath("//button[@aria-label='Clear Filters']");
    private static final By INCIDENT_ID_LINK = By.xpath("(//table[@id='incidents']//tr//td[2]//a)[1]");

    public static HardwareSupportPage_WXP getInstance() {
        return HARDWARE_SUPPORT_PAGE_WXP;
    }

    public CreateCasePage_WXP clickCreateCaseButton() {
        click(CREATE_CASE_BUTTON);
        return CreateCasePage_WXP.getInstance();
    }

    public HardwareSupportPage_WXP assertSuccessNotificationDisplayed() {
        softly().assertThat(waitTillDisplay(SUCCESS_NOTIFICATION).isDisplayed())
                .withFailMessage("Success Notification for case creation is not displayed with in wait time specified")
                .isTrue();

        LOGGER.info("Case submitted successfully");
        return this;
    }

    public HardwareSupportPage_WXP assertCaseIs_DisplayedUnder_HardwareSupportTab() {
        PreDefinedActions.getDriver().navigate().refresh();
        waitTillDocumentReady();
        clearFiltersIfApplied();

        String serialNumber = SerialNumberHolder.getSerialNumber();

        boolean isCaseCountOne = waitTillConditionWithRefresh(() -> {
            sendKeys(DEVICE_SERIAL_NUMBER_FILTER, serialNumber);
            return getCaseCountForSerialNumber() == 1;
        }, 120);

        assertThat(isCaseCountOne)
                .withFailMessage(String.format("Case is not displayed, for serial number %s in wait time specified", serialNumber))
                .isTrue();

        return this;
    }

    private static void clearFiltersIfApplied() {
        if (isElementDisplayed(CLEAR_FILTERS_LINK)) {
            click(CLEAR_FILTERS_LINK);
        }
    }

    private static int getCaseCountForSerialNumber() {
        waitForPeriodOf(5);
        String tempText = scrollAndFindElement(CASE_COUNT).getText();
        return Integer.parseInt(tempText.replaceAll("[^0-9]", ""));
    }

    public IncidentDetailsPage navigateToIncidentDetailPage() {
        click(INCIDENT_ID_LINK);
        return IncidentDetailsPage.getInstance();
    }
}
