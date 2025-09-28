package pages.wxp;

import com.e2e_activecare.pages.LandingPage;
import org.openqa.selenium.By;

import static com.e2e_activecare.utils.WaitUtil.clickByJS;
import static com.e2e_activecare.utils.WaitUtil.waitForPeriodOf;

public final class HomePage_WXP {

    private static final HomePage_WXP HOME_PAGE_WXP_INSTANCE = new HomePage_WXP();
    private final LeftNavigationPane_WXP navPane;

    private static final By PROFILE_LINK = By.xpath("//button[@aria-label='User Profile']");
    private static final By SIGN_OUT_BUTTON = By.xpath("//span[text()='Sign Out']/parent::button\n");

    private HomePage_WXP() {
        this.navPane = LeftNavigationPane_WXP.getInstance();
    }

    public static HomePage_WXP getInstance() {
        return HOME_PAGE_WXP_INSTANCE;
    }

    public PCPage_WXP navigateToPCPage() {
        navPane.expandLeftNavigationPaneIfNotAlreadyExpanded()
               .clickDevicesTab()
               .clickPCLink();
        return PCPage_WXP.getInstance();
    }

    public HardwareSupportPage_WXP navigateToHardwareSupportPage() {
        navPane.clickHardwareSupportTab();
        return HardwareSupportPage_WXP.getInstance();
    }

    public LandingPage signOut() {
        waitForPeriodOf(5);
        clickByJS(PROFILE_LINK);
        clickByJS(SIGN_OUT_BUTTON);
        return LandingPage.getInstance();
    }
}
