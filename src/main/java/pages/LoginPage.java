package pages;

import org.openqa.selenium.By;

import static com.e2e_activecare.utils.UIUtil.handleOptionalNotification;
import static com.e2e_activecare.utils.WaitUtil.click;
import static com.e2e_activecare.utils.WaitUtil.sendKeys;

public final class LoginPage {

    private LoginPage() {}
    private static final LoginPage LOGIN_PAGE_INSTANCE = new LoginPage();

    private static final By EMAIL_INPUT = By.xpath("//input[@id='email']");
    private static final By CONTINUE_BUTTON = By.xpath("//button[@class='dui-idp-login-button css-13h7jj0']");
    private static final By PASSWORD_INPUT = By.xpath("//input[@id='password']");
    private static final By FINAL_SIGN_IN_BUTTON = By.xpath("//button[@id='sign-in']");
    private static final By NOTIFICATION_CLOSE_BUTTON = By.xpath("//button[@type='button' and @aria-label='Close']");


    public static LoginPage getInstance() {
        return LOGIN_PAGE_INSTANCE;
    }

    private void enterEmail(String email) {
        sendKeys(EMAIL_INPUT, email);
        click(CONTINUE_BUTTON);
    }

    private void enterPassword(String password) {
        sendKeys(PASSWORD_INPUT, password);
        click(FINAL_SIGN_IN_BUTTON);
    }

    private void closeNotificationIfDisplayed() {
        handleOptionalNotification(NOTIFICATION_CLOSE_BUTTON);
    }

    public HomePage signIn(String email, String password) throws Exception {
        enterEmail(email);
        enterPassword(password);
        closeNotificationIfDisplayed();

        return HomePage.getInstance();
    }
}
