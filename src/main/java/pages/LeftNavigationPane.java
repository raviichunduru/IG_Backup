package pages;

import org.openqa.selenium.By;

import static com.e2e_activecare.utils.WaitUtil.click;

public final class LeftNavigationPane {

    private  LeftNavigationPane() {}
    private static final LeftNavigationPane LEFT_NAVIGATION_PANE_INSTANCE = new LeftNavigationPane();

    private static final By ADMIN_TAB = By.xpath("//span[text()='Administrative']");
    private static final By ORDERS_LINK = By.xpath("//span[text()='Orders']");
    private static final By DEVICES_TAB = By.xpath("//span[text()='Devices']");
    private static final By CUSTOMERS_TAB = By.xpath("//span[text()='Customers']");
    private static final By INCIDENTS_TAB = By.xpath("//span[@class='vn-side-menu__item-col-label'][text()='Incidents']/parent::a");

    public static LeftNavigationPane getInstance() {
        return LEFT_NAVIGATION_PANE_INSTANCE;
    }

    public LeftNavigationPane clickAdministrativeTab() {
        click(ADMIN_TAB);
        return this;
    }

    public void clickOrdersLink() {
        click(ORDERS_LINK);
    }

    public LeftNavigationPane clickCustomersTab() {
        click(CUSTOMERS_TAB);
        return this;
    }

    public DevicesPage clickDevicesTab() {
        click(DEVICES_TAB);
        return DevicesPage.getInstance();
    }

    public IncidentsPage clickIncidentsTab() {
        click(INCIDENTS_TAB);
        return IncidentsPage.getInstance();
    }
}
