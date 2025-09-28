package pages.wxp;

import org.openqa.selenium.By;

import static com.e2e_activecare.utils.UIUtil.isDisplayed;
import static com.e2e_activecare.utils.WaitUtil.click;

public final class LeftNavigationPane_WXP {

    private LeftNavigationPane_WXP() {}
    private static final LeftNavigationPane_WXP LEFT_NAVIGATION_PANE_WXP_INSTANCE = new LeftNavigationPane_WXP();

    private static final By EXPAND_BUTTON = By.xpath("//button[@id='wex_sm_expand_btn']");
    private static final By DEVICES_TAB = By.xpath("//li[@data-testid='menu_devices']");
    private static final By PC_LINK = By.xpath("//span[text()='PCs']/parent::a");
    private static final By HARDWARE_SUPPORT_LINK = By.xpath("//span[text()='Hardware Support']");

    public static LeftNavigationPane_WXP getInstance() {
        return LEFT_NAVIGATION_PANE_WXP_INSTANCE;
    }

    public LeftNavigationPane_WXP expandLeftNavigationPaneIfNotAlreadyExpanded() {
        if(isDisplayed(EXPAND_BUTTON)) {
            click(EXPAND_BUTTON);
        }
        return this;
    }

    public LeftNavigationPane_WXP clickDevicesTab() {
        click(DEVICES_TAB);
        return this;
    }

    public void clickPCLink() {
        click(PC_LINK);
    }

    public void clickHardwareSupportTab() {
        click(HARDWARE_SUPPORT_LINK);
    }
}
