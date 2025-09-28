package utils;

import com.basesource.utils.CSRGenerator;
import com.e2e_activecare.config.TestConfig;

import static com.e2e_activecare.context.EnvironmentHolder.getEnvironment;
import static com.e2e_activecare.pojo.context.SerialNumberHolder.*;

import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.Environment;
import com.e2e_activecare.pages.DeviceDetailsPage;
import com.e2e_activecare.pages.HomePage;
import com.e2e_activecare.pages.LandingPage;
import com.e2e_activecare.pages.OrderDetailsPage;
import com.e2e_activecare.pages.wxp.HomePage_WXP;
import com.e2e_activecare.pages.wxp.LandingPage_WXP;
import com.e2e_activecare.pojo.context.SerialNumberHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.security.auth.x500.X500Principal;
import java.io.StringWriter;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.e2e_activecare.constants.FrameWorkConstants.getPathForJSON_ToMakeIOTAPICall_ForDeviceEnrollment;
import static com.e2e_activecare.enums.DeviceStatus.ACTIVE;
import static com.e2e_activecare.enums.IssueCategory.BATTERY;
import static com.e2e_activecare.enums.OrderStatus.COMPLETED;
import static com.e2e_activecare.enums.OrderStatus.NEW;
import static com.e2e_activecare.listeners.LaunchDarklySuiteListener.REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED;
import static com.e2e_activecare.utils.BusinessLogicUtil.getTenantId;
import static com.e2e_activecare.utils.JSONUtil.readJsonFromFile;
import static com.e2e_activecare.utils.ShellUtil.runDeviceEnrollFlow;
import static io.restassured.http.ContentType.JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

public final class FunctionalCUJUtil {

    private FunctionalCUJUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(FunctionalCUJUtil.class);

    private static final String ROOT_ADMIN_EMAIL = TestConfig.get("rootAdminEmail");
    private static final String ROOT_ADMIN_PASSWORD = TestConfig.get("rootPassword");
    private static final String ACHT_TENANT_EMAIL = TestConfig.get("achtTenantEmail");
    private static final String ACHT_TENANT_PASSWORD = TestConfig.get("achtTenantPassword");
    private static final String DESTINATION_TENANT_EMAIL = TestConfig.get("destinationTenantEmail");
    private static final String DESTINATION_TENANT_PASSWORD = TestConfig.get("destinationTenantPassword");
    private static final String WXP_TENANT_EMAIL = TestConfig.get("wxpEuTenantEmail");
    private static final String WXP_TENANT_PASSWORD = TestConfig.get("wxpEuTenantPassword");

    @SneakyThrows
    public static void deviceImport() {
        String serialNumber =
                LandingPage.getInstance()
                        .acceptPrivacyNotification()
                        .clickSignInButton()
                        .signIn(ROOT_ADMIN_EMAIL, ROOT_ADMIN_PASSWORD)
                        .navigateToOrdersPage()
                        .uploadCSV()
                        .assertUploadSuccessful()
                        .assertUploadedFileIsDisplayedUnderOrders()
                        .assertOrderStatusIs(NEW)
                        .assert_OrderStatus_IsChangedTo_Completed_AfterCronJobRun()
                        .navigateToOrderDetailPage()
                        .assert_DeviceRegistrationInfo_In_OrderDetailsPage_MatchesWith_CSVUploaded()
                        .assertAllDevicesRegisteredSuccessful()
                        .getRegisteredDeviceSerialNumber();

        LOGGER.info("Registered Device with serial number : {}", serialNumber);
        SerialNumberHolder.setSerialNumber(serialNumber);
    }

    @SneakyThrows
    public static void deviceImportWithMultipleOrderTypes() {
        deviceImport();
        LOGGER.info("Importing same serial number {} with OOBR order type to validate multi plan allocation", getSerialNumber());

        if(getEnvironment().equals(Environment.US_STABLE) || getEnvironment().equals(Environment.US_STAGING)) {
            OrderDetailsPage.getInstance()
                    .navigateBackToOrdersPage()
                    .uploadCSV()
                    .assertUploadSuccessful()
                    .assertUploadedFileIsDisplayedUnderOrders()
                    .assertOrderStatusIs(NEW)
                    .assert_OrderStatus_IsChangedTo_Completed_AfterCronJobRun()
                    .navigateToOrderDetailPage()
                    .assert_DeviceRegistrationInfo_In_OrderDetailsPage_MatchesWith_CSVUploaded()
                    .assertAllDevicesRegisteredSuccessful();
        } else if (getEnvironment().equals(Environment.EU_STABLE) || getEnvironment().equals(Environment.EU_STAGING)) {
            HomePage.getInstance()
                    .signOut();

            LandingPage.getInstance()
                       .loadUSDASSPortal();

            deviceImport();
        }
    }

    @SneakyThrows
    public static void validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant() {
        HomePage.getInstance()
                .signOut()
                .clickSignInButton()
                .signIn(ACHT_TENANT_EMAIL, ACHT_TENANT_PASSWORD)
                .navigateToPendingEnrollmentSectionAsTenant()
                .assertRegisteredDevice_IsDisplayed_To_ACHTTenant()
                .assertRegisteredDevice_Status()
                .assertDeviceIsAssociatedWith_RightPlan();
    }

    @SneakyThrows
    public static void deviceTransfer() {

        LOGGER.info("Transferring SerialNumber to named tenant : {}", getSerialNumber());

        HomePage.getInstance()
                .signOut()
                .clickSignInButton()
                .signIn(ROOT_ADMIN_EMAIL, ROOT_ADMIN_PASSWORD)
                .navigateToOrdersPage()
                .uploadCSV_ToProcess_DeviceTransfer_OrderType()
                .assertUploadSuccessful()
                .assertUploadedFileIsDisplayedUnderOrders()
                .assertOrderStatusIs(COMPLETED)
                .navigateToOrderDetailPage()
                .assert_DeviceTransferInfo_In_OrderDetailsPage_MatchesWith_CSVUploaded()
                .assertAllDevicesTransferredSuccessfully();
    }

    @SneakyThrows
    public static void tenantValidations() {
        HomePage.getInstance()
                .signOut()
                .clickSignInButton()
                .signIn(DESTINATION_TENANT_EMAIL, DESTINATION_TENANT_PASSWORD)
                .navigateToPendingEnrollmentSectionAsTenant()
                .assertRegisteredDevice_IsDisplayed_To_DestinationTenant()
                .assertRegisteredDevice_Status()
                .assertDeviceIsAssociatedWith_RightPlan();
    }

    @SneakyThrows
    public static void deviceEnrollment() {
        deviceImport();
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            validateIfRegisteredDevice_IsDisplayedTo_ACHTTenant();
            deviceTransfer();
        }
        tenantValidationsForEnrollmentFlow();
        deleteDevice();

        runDeviceEnrollFlow(List.of(getSerialNumber()));
        wxpTenantValidations();
    }

    @SneakyThrows
    public static void tenantValidationsForEnrollmentFlow() {
        HomePage.getInstance()
                .signOut()
                .clickSignInButton()
                .signIn(DESTINATION_TENANT_EMAIL, DESTINATION_TENANT_PASSWORD)
                .navigateToPendingEnrollmentSectionAsTenant()
                .assertRegisteredDevice_IsDisplayed_To_DestinationTenant()
                .assertDeviceIsAssociatedWith_RightPlan();
    }

    public static void deleteDevice() {
        DeviceDetailsPage.getInstance()
                .navigateBackToDevicePage()
                .deleteDeviceForDestinationTenant()
                .signOut();
    }

    public static void wxpTenantValidations() {
        LandingPage_WXP.getInstance()
                .loadWXPPortal()
                .clickSignInButton()
                .signIn(WXP_TENANT_EMAIL,WXP_TENANT_PASSWORD)
                .navigateToPCPage()
                .assertDevice_IsDisplayed_To_EUTenant()
                .assertDeviceStatusIs(ACTIVE)
                .assertDevicePlans();
    }

    public static void incidentCreation() {
        LOGGER.info("Done with tenant validations. Now proceeding with case creation for : {}", getSerialNumber());

        HomePage.getInstance()
                .navigateToIncidentsPage()
                .clickCreateCaseButton()
                .createCaseFor(BATTERY)
                .assertSuccessNotificationDisplayed()
                .assertCaseIsDisplayedUnderIncidentsTab()
                .navigateToIncidentDetailPage()
                .assertFieldsIn_IncidentDetailsPage();
    }

    public static void incidentCreationInWXP() {
        HomePage_WXP.getInstance()
                .navigateToHardwareSupportPage()
                .clickCreateCaseButton()
                .createCaseFor(BATTERY)
                .assertSuccessNotificationDisplayed()
                .assertCaseIs_DisplayedUnder_HardwareSupportTab()
                .navigateToIncidentDetailPage()
                .assertFieldsIn_IncidentDetailsPage();
    }

    @SneakyThrows
    public static void performDeviceEnrollment_By_IOT_APICall() {
        String serialNumber = getSerialNumber();
        String deviceId = TestContextHolder.getDataHolder().getDeviceId();
        String tenantId = getTenantId();
        String cpin=TestConfig.get("cpin");

        String accessToken = fetchAccessToken(cpin, deviceId);
        KeyPair keyPair = generateKeyPair();
        String csr = generateCsr(deviceId, keyPair);
        CSRGenerator.authenticate(deviceId, keyPair);

        String requestBody = buildIotEnrollmentRequest(serialNumber, deviceId, tenantId, accessToken, csr);
        Response response = sendIotEnrollmentRequest(requestBody);

        assertThat(response.getStatusCode())
                .withFailMessage("IOT Api call failed, expected response code : {}, actual response code : {}",SC_OK,response.getStatusCode())
                .isEqualTo(SC_OK);
    }

    private static String fetchAccessToken(String cpin, String deviceId) {
        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("pinType", "cpin");
        jsonBody.put("pin", cpin);
        jsonBody.put("deviceId", deviceId);

        Response response = RestAssured
                .given()
                .contentType(JSON)
                .body(jsonBody)
                .post(TestConfig.get("iotDiscoveryUrl"));

        response.prettyPrint();

        return response.jsonPath().getString("service_map_data[0].service_jwt_token");
    }

    @SneakyThrows
    private static KeyPair generateKeyPair() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        return kpGen.generateKeyPair();
    }

    @SneakyThrows
    private static String generateCsr(String deviceId, KeyPair pair) {
        PKCS10CertificationRequest request = new PKCS10CertificationRequest(
                "SHA256withRSA",
                new X500Principal("CN=" + deviceId),
                pair.getPublic(),
                new DERSet(),
                pair.getPrivate()
        );

        PemObject pemObject = new PemObject("CERTIFICATE REQUEST", request.getEncoded());
        StringWriter str = new StringWriter();
        PemWriter pemWriter = new PemWriter(str);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        str.close();
        Base64.Encoder base64 = Base64.getEncoder();
        return base64.encodeToString(str.toString().getBytes());
    }

    @SneakyThrows
    private static String buildIotEnrollmentRequest(String serialNumber, String deviceId, String tenantId, String accessToken, String csr) {
        JsonNode rootNode = readJsonFromFile(getPathForJSON_ToMakeIOTAPICall_ForDeviceEnrollment());

        ObjectNode objNode = (ObjectNode) rootNode;
        objNode.put("csr", csr);
        objNode.put("deviceId", deviceId);
        objNode.put("tenantId", tenantId);
        objNode.put("token", accessToken);

        ObjectNode dataNode = (ObjectNode) objNode.get("data");
        dataNode.put("device_id", deviceId);
        dataNode.put("name", serialNumber);
        dataNode.put("serial_number", serialNumber);

        return new ObjectMapper().writeValueAsString(objNode);
    }

    private static Response sendIotEnrollmentRequest(String body) {
        String apiUrl = TestConfig.get("iotEnrollmentUrl");
        return RestAssured
                .given()
                .contentType(JSON)
                .accept(JSON)
                .body(body)
                .post(apiUrl);
    }
}