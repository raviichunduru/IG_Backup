package pages;

import com.e2e_activecare.pojo.context.SerialNumberHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import static com.e2e_activecare.enums.IssueCategory.BATTERY;
import static com.e2e_activecare.utils.AssertUtil.assertAll;
import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.WaitUtil.getText;

public final class IncidentDetailsPage {

    private IncidentDetailsPage() {}

    private static final IncidentDetailsPage INCIDENT_DETAILS_PAGE_INSTANCE = new IncidentDetailsPage();
    private static final Logger LOGGER = LogManager.getLogger(IncidentDetailsPage.class);

    private static final By CASE_ID = By.xpath("(//div[text()='ID Number']/following-sibling::div)[1]");
    private static final By SERIAL_NUMBER = By.xpath("//div[text()='Serial Number']/following-sibling::div");
    private static final By STATE = By.xpath("//div[text()='State']/following-sibling::div");
    private static final By ISSUE_TYPE = By.xpath("//div[text()='Type']/following-sibling::div//span");
    private static final By ISSUE_SUB_TYPE = By.xpath("//div[text()='Type']/following-sibling::div//p");


    public static IncidentDetailsPage getInstance() {
        return INCIDENT_DETAILS_PAGE_INSTANCE;
    }

    public void assertFieldsIn_IncidentDetailsPage() {

        assertCaseId();
        assertSerialNumber();
        assertStatus();
        assertIssueCategory();

        assertAll();
        LOGGER.info("Fields in incident details page are validated successfully");
    }

    private static void assertCaseId() {
        String caseId = getText(CASE_ID);
        softly().assertThat(caseId)
                .withFailMessage("Case ID is either blank or empty ")
                .isNotEmpty()
                .isNotBlank();
    }

    private static void assertSerialNumber() {
        String expectedSerialNumber = SerialNumberHolder.getSerialNumber();
        String actualSerialNumber = getText(SERIAL_NUMBER);

        softly().assertThat(actualSerialNumber)
                .withFailMessage(String.format("Expected Serial Number : %s , Actual Serial Number :%s",expectedSerialNumber,actualSerialNumber))
                .isEqualTo(expectedSerialNumber);
    }

    private static void assertStatus() {
        String actualStatus = getText(STATE);
        softly().assertThat(actualStatus)
                .withFailMessage(String.format("Expected Status : New , Actual Status :%s",actualStatus))
                .isEqualTo("New");
    }

    private static void assertIssueCategory() {
        String actualIssueCategory = getText(ISSUE_TYPE) + " - " + getText(ISSUE_SUB_TYPE);
        String expectedIssueCategory = BATTERY.getValue();
        softly().assertThat(actualIssueCategory)
                .withFailMessage(String.format("Expected Issue Category : %s , Actual Issue Category : %s",expectedIssueCategory, actualIssueCategory))
                .isEqualTo(expectedIssueCategory);
    }
}
