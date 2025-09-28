package pages;


import com.e2e_activecare.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import static com.e2e_activecare.utils.DynamicXpathUtil.getDynamicXpath;
import static com.e2e_activecare.utils.FileUtil.getUploadedFileName;
import static com.e2e_activecare.utils.WaitUtil.*;
import static com.e2e_activecare.utils.WaitUtil.waitTillDisplayForBellNotification;
import static org.assertj.core.api.Assertions.assertThat;

public final class HomePage {

    private static final HomePage HOME_PAGE_INSTANCE = new HomePage();
    private static final Logger LOGGER = LogManager.getLogger(HomePage.class);

    private static final By PROFILE_LINK = By.xpath("//div[@aria-label='Avatar']");
    private static final By SIGN_OUT_BUTTON = By.xpath("//button[@label='Sign Out']\n");

    private static final By BELL_ICON = By.xpath("//span[@class='notification-center-content-appbar']/button");
    private static final String BELL_NOTIFICATION_FOR_PARTNER_DEVICE_TRANSFER = "//span[contains(text(), 'Device transfer completed for %s')]";

    private final LeftNavigationPane navPane;

    private HomePage() {
        this.navPane = LeftNavigationPane.getInstance();
    }

    public static HomePage getInstance() {
        return HOME_PAGE_INSTANCE;
    }

    public OrdersPage navigateToOrdersPage() {
        navPane.clickAdministrativeTab()
                .clickOrdersLink();
        return OrdersPage.getInstance();
    }

    public DevicesPage navigateToPendingEnrollmentSectionAsPartner() {
        navPane.clickCustomersTab()
                .clickDevicesTab()
                .clickPendingEnrollmentTab();
        return DevicesPage.getInstance();
    }

    public DevicesPage navigateToPendingEnrollmentSectionAsMSP() {
        navPane.clickCustomersTab()
                .clickDevicesTab()
                .clickPendingEnrollmentTab();
        return DevicesPage.getInstance();
    }

    public LandingPage signOut() {
        waitForPeriodOf(5);
        clickByJS(PROFILE_LINK);
        clickByJS(SIGN_OUT_BUTTON);
        return LandingPage.getInstance();
    }

    public DevicesPage navigateToPendingEnrollmentSectionAsTenant() {
        return this.navPane.clickDevicesTab()
                   .clickPendingEnrollmentTab();
    }

    public IncidentsPage navigateToIncidentsPage () {
        return this.navPane.clickIncidentsTab();
    }

    public static DevicesPage assertPartnerDeviceTransferIsSuccessful() {
        By xpathForNotification = getDynamicXpath(BELL_NOTIFICATION_FOR_PARTNER_DEVICE_TRANSFER, getUploadedFileName());

        boolean isPartnerDeviceTransferSuccessful = waitTillConditionWithRefresh(() -> {
            clickByJS(BELL_ICON);
            return waitTillDisplayForBellNotification(xpathForNotification).isDisplayed();
        }, 120);

        assertThat(isPartnerDeviceTransferSuccessful)
                .withFailMessage("Bell notification for partner device transfer not displayed within maximum wait time set")
                .isTrue();

        LOGGER.info("Bell notification displayed indicating partner device transfer operation completed");
        return DevicesPage.getInstance();
    }
}
