package pages.wxp;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.config.TestConfig;
import org.openqa.selenium.By;

import static com.e2e_activecare.utils.WaitUtil.click;

public final class LandingPage_WXP {

    private LandingPage_WXP() {}
    private static final LandingPage_WXP LANDING_PAGE_WXP_INSTANCE = new LandingPage_WXP();

    private static final By SIGN_IN_LINK = By.xpath("//a[text()='Sign in']");

    public static LandingPage_WXP getInstance() {
        return LANDING_PAGE_WXP_INSTANCE;
    }

    public LandingPage_WXP loadWXPPortal() {
        PreDefinedActions.getDriver().navigate().to(TestConfig.get("wxpBaseUrl"));
        return this;
    }

    public LoginPage_WXP clickSignInButton() {
        click(SIGN_IN_LINK);
        return LoginPage_WXP.getInstance();
    }
}

