package pages;

import static com.e2e_activecare.utils.WaitUtil.isElementDisplayed;
import static com.e2e_activecare.utils.WaitUtil.click;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WindowType;

public final class LandingPage {

    private LandingPage() {}
    private static final LandingPage LANDING_PAGE_INSTANCE = new LandingPage();

    private static final By PRIVACY_ACCEPT_BUTTON = By.xpath("//button[@id='onetrust-accept-btn-handler']");
    private static final By SIGN_IN_BUTTON = By.xpath("//span[text()='Sign In']/parent::button");

    public static LandingPage getInstance() {
        return LANDING_PAGE_INSTANCE;
    }

    public void loadDASSPortal() {
        PreDefinedActions.getDriver().switchTo().newWindow(WindowType.WINDOW);
        PreDefinedActions.getDriver().get(TestConfig.get("dassBaseUrl"));
    }

    public void loadUSDASSPortal() {
        PreDefinedActions.getDriver().switchTo().newWindow(WindowType.WINDOW);
        PreDefinedActions.getDriver().get(TestConfig.get("dassBaseUrlForUS"));
    }

    public LandingPage acceptPrivacyNotification() {
        if (isElementDisplayed(PRIVACY_ACCEPT_BUTTON)) {
            click(PRIVACY_ACCEPT_BUTTON);
        }
        return this;
    }

    public LoginPage clickSignInButton() {
        click(SIGN_IN_BUTTON);
        return LoginPage.getInstance();
    }
}
