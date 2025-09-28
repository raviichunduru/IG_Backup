package pages.wxp;

import com.e2e_activecare.enums.Environment;
import org.openqa.selenium.By;

import static com.e2e_activecare.context.EnvironmentHolder.getEnvironment;
import static com.e2e_activecare.utils.UIUtil.handleOptionalNotification;
import static com.e2e_activecare.utils.WaitUtil.click;
import static com.e2e_activecare.utils.WaitUtil.sendKeys;

public final class LoginPage_WXP {

    private LoginPage_WXP() {}
    private static final LoginPage_WXP LOGIN_PAGE_WXP_INSTANCE = new LoginPage_WXP();

    private static final By EMAIL_INPUT = By.xpath("//input[@name='email']");
    private static final By CONTINUE_BUTTON = By.xpath("//span[text()='Continue']/parent::button");
    private static final By PASSWORD_INPUT = By.xpath("//input[@id='password']");
    private static final By SIGN_IN_BUTTON = By.xpath("//button[@id='sign-in']");
    private static final By SIGN_IN_BUTTON_STABLE = By.xpath("//span[text()='Sign in']/parent::button");
    private static final By NOTIFICATION_CLOSE_BUTTON = By.xpath("//button[@type='button' and @aria-label='Close']");


    public static LoginPage_WXP getInstance() {
        return LOGIN_PAGE_WXP_INSTANCE;
    }

    private void enterEmail(String email) {
        sendKeys(EMAIL_INPUT, email);
        click(getSignInButtonLocatorForEnv());
    }

    private By getSignInButtonLocatorForEnv() {
        Environment environment = getEnvironment();
        switch (environment) {
            case US_STABLE:
            case EU_STABLE:
                return SIGN_IN_BUTTON_STABLE;
            case US_STAGING:
            case EU_STAGING:
                return CONTINUE_BUTTON;
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }

    private void enterPassword(String password) {
        sendKeys(PASSWORD_INPUT, password);
        click(SIGN_IN_BUTTON);
    }

    private void closeNotificationIfDisplayed() {
        handleOptionalNotification(NOTIFICATION_CLOSE_BUTTON);
    }

    public HomePage_WXP signIn(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        closeNotificationIfDisplayed();
        return HomePage_WXP.getInstance();
    }
}
