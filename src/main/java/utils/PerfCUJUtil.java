package utils;

import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.pages.DevicesPage;
import com.e2e_activecare.pages.HomePage;
import com.e2e_activecare.pages.LandingPage;
import com.e2e_activecare.pages.OrderDetailsPage;
import com.e2e_activecare.pages.wxp.LandingPage_WXP;
import com.e2e_activecare.pages.wxp.PCPage_WXP;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.e2e_activecare.enums.OrderStatus.COMPLETED;
import static com.e2e_activecare.enums.OrderStatus.NEW;
import static com.e2e_activecare.utils.BusinessLogicUtil.getNumberOfDevices;
import static com.e2e_activecare.utils.BusinessLogicUtil.getTenantCredentialsToUse;
import static com.e2e_activecare.utils.WaitUtil.waitForPeriodOf;

public final class PerfCUJUtil {

    private PerfCUJUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(PerfCUJUtil.class);

    private static final String ROOT_ADMIN_EMAIL = TestConfig.get("rootAdminEmail");
    private static final String ROOT_ADMIN_PASSWORD = TestConfig.get("rootPassword");
    private static final String MSP_ADMIN_EMAIL = TestConfig.get("mspAdminEmail");
    private static final String MSP_ADMIN_PASSWORD = TestConfig.get("mspAdminPassword");
    private static final String WXP_TENANT_EMAIL = TestConfig.get("wxpEuTenantEmail");
    private static final String WXP_TENANT_PASSWORD = TestConfig.get("wxpEuTenantPassword");

    private static final boolean isBulkDeleteEnabled = TestConfig.getBoolean("bulkDeleteOfDevices");

    @SneakyThrows
    public static void cleanUpOfExistingDevicesAsMSP() {
        LOGGER.info("deleting devices if exists, as MSP");

        LandingPage.getInstance()
                .acceptPrivacyNotification()
                .clickSignInButton()
                .signIn(MSP_ADMIN_EMAIL, MSP_ADMIN_PASSWORD)
                .navigateToPendingEnrollmentSectionAsMSP()
                .deleteExistingDevicesBeforeTest()
                .signOut();
    }

    @SneakyThrows
    public static void deviceImport() {
        LandingPage.getInstance()
                .acceptPrivacyNotification()
                .clickSignInButton()
                .signIn(ROOT_ADMIN_EMAIL, ROOT_ADMIN_PASSWORD)
                .navigateToOrdersPage()
                .uploadDeviceImportCSV_WithLoad()
                .assertUploadSuccessful()
                .assertUploadedFileIsDisplayedUnderOrders()
                .assertOrderStatusIs(NEW)
                .assert_OrderStatus_IsChangedTo_Completed_AfterCronJobRun()
                .navigateToOrderDetailPage()
                .assertAllDevicesRegisteredSuccessfulForLoadTest();

        LOGGER.info("Device import with load is completed");
    }

    @SneakyThrows
    public static void deviceImportWithMultipleOrderTypes() {
        deviceImport();
        LOGGER.info("Importing same {} serial numbers with OOBR order type to validate multi plan allocation", getNumberOfDevices());

        OrderDetailsPage.getInstance()
                .navigateBackToOrdersPage()
                .uploadDeviceImportCSV_WithLoad()
                .assertUploadSuccessful()
                .assertUploadedFileIsDisplayedUnderOrders()
                .assertOrderStatusIs(NEW)
                .assert_OrderStatus_IsChangedTo_Completed_AfterCronJobRun()
                .navigateToOrderDetailPage()
                .assertAllDevicesRegisteredSuccessfulForLoadTest();
    }

    @SneakyThrows
    public static void deviceTransfer() {
        HomePage.getInstance()
                .signOut()
                .clickSignInButton()
                .signIn(ROOT_ADMIN_EMAIL, ROOT_ADMIN_PASSWORD)
                .navigateToOrdersPage()
                .uploadDeviceTransferCSV_WithLoad()
                .assertUploadSuccessful()
                .assertUploadedFileIsDisplayedUnderOrders()
                .assertOrderStatusIs(COMPLETED)
                .navigateToOrderDetailPage()
                .assertAllDevicesTransferredSuccessfulForLoadTest();

        LOGGER.info("Device transfer with load is completed");
    }

    @SneakyThrows
    public static void tenantValidations() {

        String[] tenantCredentials = getTenantCredentialsToUse();
        String tenantEmail = tenantCredentials[0];
        String tenantPassword = tenantCredentials[1];

        int numberOfDevices = getNumberOfDevices();

        HomePage.getInstance()
                .signOut()
                .clickSignInButton()
                .signIn(tenantEmail, tenantPassword)
                .navigateToPendingEnrollmentSectionAsTenant()
                .assertCountOfDevices(numberOfDevices)
                .assertAllDevicesTransferred_AssociatedWith_RightPlan();
    }

    @SneakyThrows
    public static void cleanUpOfDevices() {
        if (isBulkDeleteEnabled) {
            LOGGER.info("Performing bulk device delete operation by MSP");

            HomePage.getInstance()
                    .signOut()
                    .clickSignInButton()
                    .signIn(MSP_ADMIN_EMAIL, MSP_ADMIN_PASSWORD)
                    .navigateToPendingEnrollmentSectionAsMSP()
                    .deleteDevicesAsMSP()
                    .assertThatMSPDeletedDevicesSuccessfully()
                    .signOut();
        }
    }

    @SneakyThrows
    public static void deleteDevicesAsMSP() {
        LOGGER.info("Proceeding to perform device delete as MSP");

        HomePage.getInstance()
                .signOut()
                .clickSignInButton()
                .signIn(MSP_ADMIN_EMAIL, MSP_ADMIN_PASSWORD)
                .navigateToPendingEnrollmentSectionAsMSP()
                .deleteDevicesAsMSP()
                .assertThatMSPDeletedDevicesSuccessfully();
    }

    @SneakyThrows
    public static void performAssetUploadAsMSP() {
        LOGGER.info("Proceeding with asset upload and related validations");
        int numberOfDevices = getNumberOfDevices();

        DevicesPage.getInstance()
                .navigateToAllDevicesSection()
                .performAssetUpload()
                .clickPendingEnrollmentTab()
                .assertCountOfDevicesAddedByMSP(numberOfDevices)
                .signOut();
    }

    @SneakyThrows
    public static void tenantValidationsAfterMSPUpload() {
        LOGGER.info("Asset upload as MSP is done. Now proceeding with tenant validations");

        String[] tenantCredentials = getTenantCredentialsToUse();
        String tenantEmail = tenantCredentials[0];
        String tenantPassword = tenantCredentials[1];

        int numberOfDevices = getNumberOfDevices();

        LandingPage.getInstance()
                .clickSignInButton()
                .signIn(tenantEmail, tenantPassword)
                .navigateToPendingEnrollmentSectionAsTenant()
                .assertCountOfDevices(numberOfDevices)
                .assertAllDevicesTransferred_AssociatedWith_RightPlan();
    }

    @SneakyThrows
    public static void cleanUpOfExistingDevicesAsWxpTenant() {
        LOGGER.info("deleting devices if exists, as wxp tenant");

        LandingPage_WXP.getInstance()
                .loadWXPPortal()
                .clickSignInButton()
                .signIn(WXP_TENANT_EMAIL,WXP_TENANT_PASSWORD)
                .navigateToPCPage()
                .deleteExistingDevicesBeforeTest()
                .signOut();
    }

    @SneakyThrows
    public static void wxpTenantValidations() {
        HomePage.getInstance()
                .signOut();

        LOGGER.info("CPIN device enrollment is done. Now proceeding with tenant validations");
        int numberOfDevices = getNumberOfDevices();
        waitForPeriodOf(5);

        LandingPage_WXP.getInstance()
                .loadWXPPortal()
                .clickSignInButton()
                .signIn(WXP_TENANT_EMAIL,WXP_TENANT_PASSWORD)
                .navigateToPCPage()
                .assertCountOfDevices(numberOfDevices)
                .assertAllDevicesEnrolledAre_AssociatedWith_ActiveCarePlan();
    }

    @SneakyThrows
    public static void cleanUpOfDevicesAsWxpTenant() {
        if (isBulkDeleteEnabled) {
            LOGGER.info("Performing bulk device delete as WXP tenant");

            PCPage_WXP.getInstance()
                    .deleteDevicesAsWxpTenant()
                    .assertThatWxpTenantDeletedDevicesSuccessfully();
        }
    }
}
