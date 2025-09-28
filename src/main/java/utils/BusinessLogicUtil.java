package utils;

import com.e2e_activecare.annotations.SkipSNRInsert;
import com.e2e_activecare.annotations.TestMetadata;
import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.*;

import static com.e2e_activecare.context.EnvironmentHolder.getEnvironment;
import static com.e2e_activecare.context.TestContextHolder.getCsvUploadCount;
import static com.e2e_activecare.context.TestContextHolder.getCurrentTestType;
import static com.e2e_activecare.enums.Environment.*;
import static com.e2e_activecare.listeners.LaunchDarklySuiteListener.REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED;
import static com.e2e_activecare.utils.DateUtil.formatDate;
import static com.e2e_activecare.utils.FileUtil.getUploadedFileName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;

public final class BusinessLogicUtil {

    private BusinessLogicUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(BusinessLogicUtil.class);

    public static String getTenantId() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_ENROLLMENT:
                return TestConfig.get("wxpTenantId");
            case DEVICE_IMPORT_TRANSFER:
                return TestConfig.get("tenantId");
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String getTenantName() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_ENROLLMENT:
                return TestConfig.get("wxpTenantName");
            case DEVICE_IMPORT_TRANSFER:
                return TestConfig.get("tenantName");
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static List<String> getPlanIDs() {
        OrderType[] orderTypes = TestContextHolder.getCurrentOrderTypes();
        List<String> planIds = new ArrayList<>();
        for (OrderType orderType : orderTypes) {
            switch (orderType) {
                case ACTIVE_CARE:
                case PREMIUM:
                case PREMIUM_PLUS:
                    planIds.add(TestConfig.get("hpActiveCarePlanId"));
                    break;
                case OUT_OF_BAND_REMEDIATION:
                    planIds.add(TestConfig.get("hpRemoteCarePlanId"));
                    break;
                default:
                    throw new IllegalStateException("Unexpected order type: " + orderType);
            }
        }
        return planIds;
    }

    public static ArrayList<String> getServiceNamesDynamodb() {
        TestType testType = getCurrentTestType();
        OrderType[] orderTypes = TestContextHolder.getCurrentOrderTypes();
        Set<String> serviceNames = new LinkedHashSet<>(); // preserves order, avoids duplicates

        switch (testType) {
            case DEVICE_ENROLLMENT:
                for (OrderType orderType : orderTypes) {
                    String configKey;
                    switch (orderType) {
                        case ACTIVE_CARE:
                            configKey = "deviceEnrollment_ActiveCareOrderType_ServiceName_DynamoDB";
                            break;
                        case PREMIUM:
                            configKey = "deviceEnrollment_PremiumOrderType_ServiceName_DynamoDB";
                            break;
                        case PREMIUM_PLUS:
                            configKey = "deviceEnrollment_PremiumPlusOrderType_ServiceName_DynamoDB";
                            break;
                        case OUT_OF_BAND_REMEDIATION:
                            configKey = "deviceEnrollment_OOBROrderType_ServiceName_DynamoDB";
                            break;
                        default:
                            throw new IllegalStateException("Unexpected order type for DEVICE_ENROLLMENT: " + orderType);
                    }
                    String value = TestConfig.get(configKey);
                    if (value != null && !value.isEmpty()) {
                        for (String name : value.split(",")) {
                            serviceNames.add(name.trim());
                        }
                    }
                }
                break;
            case DEVICE_IMPORT_TRANSFER:
                for (OrderType orderType : orderTypes) {
                    String configKey = isOutOfBandRemediation(orderType)
                            ? "deviceImport_OOBROrderType_ServiceName_DynamoDB"
                            : "deviceImport_Non_OOBROrderType_ServiceName_DynamoDB";
                    String value = TestConfig.get(configKey);
                    if (value != null && !value.isEmpty()) {
                        for (String name : value.split(",")) {
                            serviceNames.add(name.trim());
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
        return new ArrayList<>(serviceNames);
    }

    public static List<String> getPlanNamesMongodb() {
        TestType testType = getCurrentTestType();
        OrderType[] orderTypes = TestContextHolder.getCurrentOrderTypes();
        Set<String> planNames = new LinkedHashSet<>();

        switch (testType) {
            case DEVICE_ENROLLMENT:
                for (OrderType orderType : orderTypes) {
                    String configKey;
                    switch (orderType) {
                        case ACTIVE_CARE:
                            configKey = "deviceEnrollment_ActiveCareOrderType_PlanName_MongoDB";
                            break;
                        case PREMIUM:
                            configKey = "deviceEnrollment_PremiumOrderType_PlanName_MongoDB";
                            break;
                        case PREMIUM_PLUS:
                            configKey = "deviceEnrollment_PremiumPlusOrderType_PlanName_MongoDB";
                            break;
                        case OUT_OF_BAND_REMEDIATION:
                            configKey = "deviceEnrollment_OOBROrderType_PlanName_MongoDB";
                            break;
                        default:
                            throw new IllegalStateException("Unexpected order type for DEVICE_ENROLLMENT: " + orderType);
                    }
                    String value = TestConfig.get(configKey);
                    if (value != null && !value.isEmpty()) {
                        for (String name : value.split(",")) {
                            planNames.add(name.trim());
                        }
                    }
                }
                break;
            case DEVICE_IMPORT_TRANSFER:
                for (OrderType orderType : orderTypes) {
                    String configKey = isOutOfBandRemediation(orderType)
                            ? "deviceImport_OOBROrderType_PlanName_MongoDB"
                            : "deviceImport_Non_OOBROrderType_PlanName_MongoDB";
                    String value = TestConfig.get(configKey);
                    if (value != null && !value.isEmpty()) {
                        for (String name : value.split(",")) {
                            planNames.add(name.trim());
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
        return new ArrayList<>(planNames);
    }

    public static List<String> getPlanValuesMongodb() {
        TestType testType = getCurrentTestType();
        OrderType[] orderTypes = TestContextHolder.getCurrentOrderTypes();
        Set<String> planValues = new LinkedHashSet<>();

        switch (testType) {
            case DEVICE_ENROLLMENT:
                for (OrderType orderType : orderTypes) {
                    String configKey;
                    switch (orderType) {
                        case ACTIVE_CARE:
                            configKey = "deviceEnrollment_ActiveCareOrderType_PlanValue_MongoDB";
                            break;
                        case PREMIUM:
                            configKey = "deviceEnrollment_PremiumOrderType_PlanValue_MongoDB";
                            break;
                        case PREMIUM_PLUS:
                            configKey = "deviceEnrollment_PremiumPlusOrderType_PlanValue_MongoDB";
                            break;
                        case OUT_OF_BAND_REMEDIATION:
                            configKey = "deviceEnrollment_OOBROrderType_PlanValue_MongoDB";
                            break;
                        default:
                            throw new IllegalStateException("Unexpected order type for DEVICE_ENROLLMENT: " + orderType);
                    }
                    String value = TestConfig.get(configKey);
                    if (value != null && !value.isEmpty()) {
                        for (String val : value.split(",")) {
                            planValues.add(val.trim());
                        }
                    }
                }
                break;
            case DEVICE_IMPORT_TRANSFER:
                for (OrderType orderType : orderTypes) {
                    String configKey = isOutOfBandRemediation(orderType)
                            ? "deviceImport_OOBROrderType_PlanValue_MongoDB"
                            : "deviceImport_Non_OOBROrderType_PlanValue_MongoDB";
                    String value = TestConfig.get(configKey);
                    if (value != null && !value.isEmpty()) {
                        for (String val : value.split(",")) {
                            planValues.add(val.trim());
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
        return new ArrayList<>(planValues);
    }

    private static boolean isOutOfBandRemediation(OrderType orderType) {
        return orderType == OrderType.OUT_OF_BAND_REMEDIATION;
    }

    public static String getDeviceStatusNameMongodb() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_ENROLLMENT:
                return TestConfig.get("deviceEnrollment_deviceStatusName_MongoDB");
            case DEVICE_IMPORT_TRANSFER:
                return TestConfig.get("deviceImport_deviceStatusName_MongoDB");
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String getDeviceStatusValueMongodb() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_ENROLLMENT:
                return TestConfig.get("deviceEnrollment_deviceStatusValue_MongoDB");
            case DEVICE_IMPORT_TRANSFER:
                return TestConfig.get("deviceImport_deviceStatusValue_MongoDB");
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String[] getFormattedPlanDates(String startDate, String endDate) {

        String formattedPlanStartDate = formatDate(startDate, TimeStampFormat.ISO_DATE, TimeStampFormat.UI_DATE_FORMAT_FOR_PLAN_ENROLL);
        String formattedPlanEndDate = formatDate(endDate, TimeStampFormat.ISO_DATE, TimeStampFormat.UI_DATE_FORMAT_FOR_PLAN_ENROLL);
        return new String[]{formattedPlanStartDate, formattedPlanEndDate};
    }

    public static String[] getFormattedServiceDates(String startDate, String endDate) {

        String formattedPlanStartDate = formatDate(startDate, TimeStampFormat.DB_DATE_FORMAT, TimeStampFormat.UI_DATE_FORMAT_FOR_SERVICE_DATE);
        String formattedPlanEndDate = formatDate(endDate, TimeStampFormat.DB_DATE_FORMAT, TimeStampFormat.UI_DATE_FORMAT_FOR_SERVICE_DATE);
        return new String[]{formattedPlanStartDate, formattedPlanEndDate};
    }

    public static String getExpectedDeviceRegistrationStatus() {
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            String env = System.getProperty("environment", "us").toLowerCase();
            return String.format("Adding device in %s ACHT Tenant", env.contains("us") ? "us" : "eu");
        }

        return getCsvUploadCount() > 1
                ? "Device already exists. Start date & End date are updated"
                : "Success";
    }

    public static String getExpectedDeviceTransferStatus() {
        if (REMOVE_END_CUSTOMER_NAME_TOGGLE_ENABLED) {
            return "DEVICE_ADDED";
        }
        return "EXISTS_AT_DESTINATION";
    }

    public static String getCompanyName() {
        Environment environment = getEnvironment();
        TestType currentTestType = getCurrentTestType();

        switch (environment) {
            case US_STAGING:
                return "DoNotDeleteActiveCareOnlyTenant";
            case EU_STAGING:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "donotdeleteacpiusereu";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "donotdeleteacpiwpteu1";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "DoNotDeleteActiveCareOnlyTenantEU";
                }
            default:
                throw new IllegalStateException("Unexpected environment: " + environment);
        }
    }

    public static boolean shouldSkipSNRInsert() {
        Method method = TestContextHolder.getCurrentTestMethod();
        if (method.isAnnotationPresent(SkipSNRInsert.class)) {
            LOGGER.info("Skipping SNR insertion for test: {}", method.getName());
            return true;
        }
        return false;
    }

    public static DeviceStatus getExpectedDeviceStatus() {
        try {
            Method currentTestMethod = TestContextHolder.getCurrentTestMethod();

            if (currentTestMethod.isAnnotationPresent(TestMetadata.class)) {
                TestMetadata metadata = currentTestMethod.getAnnotation(TestMetadata.class);
                return metadata.deviceStatus();
            } else {
                throw new IllegalStateException("No @TestMetadata annotation present on the current test method.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve expected device status from @TestMetadata annotation.", e);
        }
    }

    public static int getNumberOfDevices() {

        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                return TestConfig.getInt("numberOfDevicesForDeviceImportPerfCUJ");
            case DEVICE_ENROLLMENT_PERF_CUJ:
                return TestConfig.getInt("numberOfDevicesForDeviceEnrollmentPerfCUJ");
            case ASSET_UPLOAD_PERF_CUJ:
                return TestConfig.getInt("numberOfDevicesForAssetUploadPerfCUJ");
            case DEVICE_IMPORT_TRANSFER:
            case DEVICE_ENROLLMENT:
                return TestConfig.getInt("numberOfDevicesForFunctionalCUJs");
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String[] getTenantCredentialsToUse() {
        TestType testType = getCurrentTestType();
        String email;
        String password;

        switch (testType) {
            case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                email = TestConfig.get("destinationTenantEmail");
                password = TestConfig.get("destinationTenantPassword");
                break;
            case DEVICE_ENROLLMENT_PERF_CUJ:
                email = TestConfig.get("tenantEmailForDeviceEnrollPerfCUJ");
                password = TestConfig.get("tenantPasswordForDeviceEnrollPerfCUJ");
                break;
            case ASSET_UPLOAD_PERF_CUJ:
                email = TestConfig.get("tenantEmailForAssetUploadPerfCUJ");
                password = TestConfig.get("tenantPasswordForAssetUploadPerfCUJ");
                break;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
        return new String[]{email, password};
    }

    public static String getApiUrl() {

        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                return TestConfig.get("assetSearchUrlForDeviceImportCUJ");
            case DEVICE_ENROLLMENT_PERF_CUJ:
                return TestConfig.get("assetSearchUrlForDeviceEnrollmentCUJ");
            case ASSET_UPLOAD_PERF_CUJ:
                return TestConfig.get("assetSearchUrlForAssetUploadCUJ");
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static boolean isRunningTestPerfCUJ() {
        TestType testType = getCurrentTestType();
        return testType == TestType.DEVICE_IMPORT_TRANSFER_PERF_CUJ ||
                testType == TestType.DEVICE_ENROLLMENT_PERF_CUJ ||
                testType == TestType.ASSET_UPLOAD_PERF_CUJ;
    }

    public static Map<OrderType, String> getOrderTypeToPlanNameMap() {
        return Map.of(
                OrderType.ACTIVE_CARE, "HP Active Care",
                OrderType.PREMIUM, "HP Active Care",
                OrderType.PREMIUM_PLUS, "HP Active Care",
                OrderType.OUT_OF_BAND_REMEDIATION, "HP Remotecare"
        );
    }

    public static String getFileNameToLookUnderOrdersPage() {
        String uploadedFileName = getUploadedFileName();
        Environment environment = getEnvironment();

        if (environment.equals(US_STABLE) || environment.equals(US_STAGING)) {
            uploadedFileName = "US_" + uploadedFileName;
        } else if (environment.equals(EU_STABLE) || environment.equals(EU_STAGING)) {
            uploadedFileName = "EU_" + uploadedFileName;
        }
        return uploadedFileName;
    }
}