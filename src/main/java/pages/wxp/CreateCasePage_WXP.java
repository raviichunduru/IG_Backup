package pages.wxp;

import com.e2e_activecare.enums.IssueCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import static com.e2e_activecare.pojo.context.SerialNumberHolder.getSerialNumber;
import static com.e2e_activecare.utils.DynamicXpathUtil.getDynamicXpath;
import static com.e2e_activecare.utils.RandomUtil.getRandomIntegerAsString;
import static com.e2e_activecare.utils.RandomUtil.getRandomString;
import static com.e2e_activecare.utils.UIUtil.scrollAndClick;
import static com.e2e_activecare.utils.WaitUtil.*;
import static com.e2e_activecare.utils.WaitUtil.click;

public final class CreateCasePage_WXP {

    private CreateCasePage_WXP() {}
    private static final CreateCasePage_WXP CREATE_CASE_PAGE_WXP_INSTANCE = new CreateCasePage_WXP();
    private static final Logger LOGGER = LogManager.getLogger(CreateCasePage_WXP.class);

    private static final By DEVICES_DROPDOWN = By.xpath("//span[text()='Device']/parent::div");
    private static final By SEARCH_ITEMS_INPUT = By.xpath("//input[@placeholder='Search Items']");
    private static final By SHOWING_ONE_RESULT_TEXT = By.xpath("//span[text()='Showing 1 result']");
    private static final String SERIAL_NUMBER_UNDER_SEARCH_ITEMS = "//span[contains(text(), '%s')]";
    private static final By NEXT_BUTTON = By.xpath("//span[text()='Next']/parent::button");
    private static final By ISSUE_CATEGORY_DROPDOWN = By.xpath("//div[@id='select-issue-create-case']");
    private static final String ISSUE_CATEGORY_TYPE = "//span[text()='%s']";
    private static final By LOCATION_DROPDOWN = By.xpath("//div[@id='dui-create-case-select-location']");
    private static final By NEW_SHIPPING_LOCATION = By.xpath("//span[text()='New Shipping Location']");
    private static final By COUNTRY_DROPDOWN = By.xpath("//div[@id='country']");
    private static final By COUNTRY_NAME = By.xpath("//span[text()='Algeria']");
    private static final By ADDRESS_LINE1 = By.xpath("//input[@name='addressLine1']");
    private static final By CITY = By.xpath("//input[@name='city']");
    private static final By STATE = By.xpath("//input[@name='state']");
    private static final By POSTAL_CODE = By.xpath("//input[@name='postalCode']");
    private static final By PHONE_NUMBER = By.xpath("//input[@name='phoneNumber']");
    private static final By SUBMIT_BUTTON = By.xpath("//span[text()='Submit']/parent::button");


    public static CreateCasePage_WXP getInstance() {
        return CREATE_CASE_PAGE_WXP_INSTANCE;
    }

    public HardwareSupportPage_WXP createCaseFor(IssueCategory issueCategory) {
        waitTillDocumentReady();
        waitForPeriodOf(5);

        selectDevice();
        selectIssueCategory(issueCategory);
        selectLocation();
        submitCase();

        LOGGER.info("Case submitted in WXP portal for issue category type : {}",issueCategory);
        return HardwareSupportPage_WXP.getInstance();
    }

    private static void submitCase() {
        click(SUBMIT_BUTTON);
    }

    private void selectLocation() {
        click(LOCATION_DROPDOWN);
        clickByJS(NEW_SHIPPING_LOCATION);
        click(NEXT_BUTTON);

        click(COUNTRY_DROPDOWN);
        scrollAndClick(COUNTRY_NAME);
        sendKeys(ADDRESS_LINE1, getRandomString(10));
        sendKeys(CITY, getRandomString(6));
        sendKeys(STATE, getRandomString(8));
        sendKeys(POSTAL_CODE, getRandomIntegerAsString(6));
        click(NEXT_BUTTON);

        sendKeys(PHONE_NUMBER,getRandomIntegerAsString(10));
        click(NEXT_BUTTON);
    }

    private void selectIssueCategory(IssueCategory issueCategory) {
        click(ISSUE_CATEGORY_DROPDOWN);
        clickByJS(getDynamicXpath(ISSUE_CATEGORY_TYPE, issueCategory.getValue()));
        click(NEXT_BUTTON);
    }

    private void selectDevice() {
        click(DEVICES_DROPDOWN);
        String serialNumber = getSerialNumber();
        sendKeys(SEARCH_ITEMS_INPUT, serialNumber);
        waitTillDisplay(SHOWING_ONE_RESULT_TEXT);
        clickByJS(getDynamicXpath(SERIAL_NUMBER_UNDER_SEARCH_ITEMS,serialNumber));
        click(NEXT_BUTTON);
    }
}

