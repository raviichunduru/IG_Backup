import com.e2e_activecare.annotations.SkipSNRDeletion;
import com.e2e_activecare.annotations.SkipSNRInsert;
import com.e2e_activecare.annotations.TestMetadata;
import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.enums.OrderType;
import com.e2e_activecare.pages.LandingPage;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import static com.e2e_activecare.enums.DeviceStatus.*;
import static com.e2e_activecare.enums.OrderType.*;
import static com.e2e_activecare.enums.TestType.*;
import static com.e2e_activecare.listeners.LaunchDarklySuiteListener.REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED;
import static com.e2e_activecare.utils.DBUtil.fetchAndValidateDBData;
import static com.e2e_activecare.utils.FunctionalCUJUtil.validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant;
import static com.e2e_activecare.utils.FunctionalCUJUtil.*;

public final class E2EActiveCareFunctionalCUJTest extends CommonTest {

    private E2EActiveCareFunctionalCUJTest() {}

    private static final String AC_PARTNER_EMAIL = TestConfig.get("acPartnerTenantEmail");
    private static final String AC_PARTNER_PASSWORD = TestConfig.get("acPartnerTenantPassword");
    private static final String AC_CUSTOMER_TENANT_EMAIL = TestConfig.get("acCustomerTenantEmail");
    private static final String AC_CUSTOMER_TENANT_PASSWORD = TestConfig.get("acCustomerTenantPassword");
    private static final String ACPI_CUSTOMER_TENANT_EMAIL = TestConfig.get("acpiCustomerTenantEmail");
    private static final String ACPI_CUSTOMER_TENANT_PASSWORD = TestConfig.get("acpiCustomerTenantPassword");

    @SneakyThrows
    @TestMetadata(testType = DEVICE_IMPORT_TRANSFER, orderType = ACTIVE_CARE, deviceStatus = READY)
    @Test(groups = {"ActiveCare_Functional_CUJ"})
    public void activeCareDeviceImportAndTransferTest() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidations();
        fetchAndValidateDBData();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_IMPORT_TRANSFER, orderType = PREMIUM, deviceStatus = READY)
    @Test(groups = {"ActiveCare_Functional_CUJ"})
    public void premiumDeviceImportAndTransferTest() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidations();
        fetchAndValidateDBData();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_IMPORT_TRANSFER, orderType = PREMIUM_PLUS, deviceStatus = READY)
    @Test(groups = {"ActiveCare_Functional_CUJ"})
    public void premiumPlusDeviceImportAndTransferTest() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidations();
        fetchAndValidateDBData();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_IMPORT_TRANSFER, orderType = OUT_OF_BAND_REMEDIATION, deviceStatus = READY)
    @Test(groups = {"ActiveCare_Functional_CUJ"})
    public void OOBRDeviceImportAndTransferTest() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidations();
        fetchAndValidateDBData();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_ENROLLMENT, orderType = ACTIVE_CARE, deviceStatus = ACTIVE)
    @Test(description = "Disabled due to JIRA bug: BUG-3413", groups = {"ActiveCare_Functional_CUJ"})
    public void activeCareDeviceEnrollmentTest() {
        deviceEnrollment();
        //commented due to bug#https://hp-jira.external.hp.com/browse/WEXPANDA-1400
        //fetchAndValidateDBData();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_ENROLLMENT, orderType = PREMIUM, deviceStatus = ACTIVE)
    @Test(description = "Disabled due to JIRA bug: BUG-3413", groups = {"ActiveCare_Functional_CUJ"})
    public void premiumDeviceEnrollmentTest() {
        deviceEnrollment();
        //commented due to bug#https://hp-jira.external.hp.com/browse/WEXPANDA-1400
        //fetchAndValidateDBData();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_ENROLLMENT, orderType = PREMIUM_PLUS, deviceStatus = ACTIVE)
    @Test(description = "Disabled due to JIRA bug: BUG-3413", groups = {"ActiveCare_Functional_CUJ"})
    public void premiumPlusDeviceEnrollmentTest() {
        deviceEnrollment();
        //commented due to bug#https://hp-jira.external.hp.com/browse/WEXPANDA-1400
        //fetchAndValidateDBData();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_ENROLLMENT, orderType = OUT_OF_BAND_REMEDIATION, deviceStatus = ACTIVE)
    @Test(description = "Disabled due to JIRA bug: BUG-3413", groups = {"ActiveCare_Functional_CUJ"})
    public void OOBRDeviceEnrollmentTest() {
        deviceEnrollment();
        //commented due to bug#https://hp-jira.external.hp.com/browse/WEXPANDA-1400
        //fetchAndValidateDBData();
    }

    @SneakyThrows
    @SkipSNRDeletion
    @TestMetadata(testType = PARTNER_DEVICETRANSFER_AC_TO_ACPI, orderType = OrderType.PARTNER_DEVICE_TRANSFER_AC_TO_ACPI)
    @Test(enabled = true, groups = {"ActiveCare_Functional_CUJ"})
    public void partnerDeviceTransferFromAcToAcpiCustomerTest() {
        LandingPage.getInstance()
                .acceptPrivacyNotification()
                .clickSignInButton()
                .signIn(AC_PARTNER_EMAIL,AC_PARTNER_PASSWORD)
                .navigateToPendingEnrollmentSectionAsPartner()
                .performDeviceTransferFromAcCustomerToAcpiCustomer()
                .assertDeviceIsTransferredToAcpiCustomer()
                .signOut()
                .clickSignInButton()
                .signIn(ACPI_CUSTOMER_TENANT_EMAIL, ACPI_CUSTOMER_TENANT_PASSWORD)
                .navigateToPendingEnrollmentSectionAsTenant()
                .assertDeviceIsDisplayedToAcpiCustomer();
    }

    @SneakyThrows
    @SkipSNRDeletion
    @TestMetadata(testType = PARTNER_DEVICETRANSFER_ACPI_TO_AC, orderType = OrderType.PARTNER_DEVICE_TRANSFER_ACPI_TO_AC)
    @Test(enabled = true, groups = {"ActiveCare_Functional_CUJ"})
    public void partnerDeviceTransferFromAcpiToAcCustomerTest() {
        LandingPage.getInstance()
                .acceptPrivacyNotification()
                .clickSignInButton()
                .signIn(AC_PARTNER_EMAIL,AC_PARTNER_PASSWORD)
                .navigateToPendingEnrollmentSectionAsPartner()
                .performDeviceTransferFromAcpiCustomerToAcCustomer()
                .assertDeviceIsTransferredToAcCustomer()
                .signOut()
                .clickSignInButton()
                .signIn(AC_CUSTOMER_TENANT_EMAIL, AC_CUSTOMER_TENANT_PASSWORD)
                .navigateToPendingEnrollmentSectionAsTenant()
                .assertDeviceIsDisplayedToAcCustomer();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_IMPORT_TRANSFER, orderType = ACTIVE_CARE, deviceStatus = READY)
    @Test(groups = {"ActiveCare_Functional_CUJ"})
    public void incidentCreation_For_ReadyStateDevice_Test() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidations();
        incidentCreation();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_ENROLLMENT, orderType = ACTIVE_CARE, deviceStatus = ACTIVE)
    @Test(enabled = false, description = "Disabled due to JIRA bug: BUG-3413", groups = {"ActiveCare_Functional_CUJ"})
    public void incidentCreation_For_ActiveStateDevice_Test() {
        deviceEnrollment();
        incidentCreationInWXP();
    }

    @SneakyThrows
    @SkipSNRInsert
    @SkipSNRDeletion
    @TestMetadata(testType = DEVICE_IMPORT_TRANSFER, orderType = ACTIVE_CARE, deviceStatus = ERROR)
    @Test(groups = {"ActiveCare_Functional_CUJ"})
    public void incidentCreation_For_ErrorStateDevice_Test() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidations();
        incidentCreation();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_ENROLLMENT, orderType = ACTIVE_CARE, deviceStatus = ACTIVE)
    @Test(description = "Disabled due to JIRA bug: BUG-3413", groups = {"ActiveCare_Functional_CUJ"})
    public void deviceEnrollmentBy_IOT_APICall() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidationsForEnrollmentFlow();
        deleteDevice();

        performDeviceEnrollment_By_IOT_APICall();
        wxpTenantValidations();
    }

    @SneakyThrows
    @TestMetadata(testType = DEVICE_IMPORT_TRANSFER, orderType = {ACTIVE_CARE,OUT_OF_BAND_REMEDIATION}, deviceStatus = READY)
    @Test(groups = {"ActiveCare_Functional_CUJ"})
    public void multiPlanValidationTest() {
        deviceImportWithMultipleOrderTypes();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidations();
        fetchAndValidateDBData();
    }
}