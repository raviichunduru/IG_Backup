package utils;

import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.TestType;
import com.e2e_activecare.enums.TimeStampFormat;
import com.e2e_activecare.pojo.DataHolderForDBValidations;
import com.e2e_activecare.pojo.DeviceReport;
import com.e2e_activecare.pojo.context.SerialNumberHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.e2e_activecare.context.TestContextHolder.getCurrentTestType;
import static com.e2e_activecare.enums.TestType.DEVICE_ENROLLMENT;
import static com.e2e_activecare.pojo.context.SerialNumberHolder.getSerialNumber;
import static com.e2e_activecare.utils.AssertUtil.assertAll;
import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.AssertUtil.clear;
import static com.e2e_activecare.utils.AzureUtil.triggerAzurePipelineToFetchDeviceReport;
import static com.e2e_activecare.utils.BusinessLogicUtil.*;
import static com.e2e_activecare.utils.DateUtil.formatDate;
import static com.e2e_activecare.utils.StringUtil.toList;
import static com.e2e_activecare.utils.WaitUtil.waitForPeriodOf;

@SuppressWarnings("LoggingSimilarMessage")
public final class DBUtil {

    private DBUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(DBUtil.class);

    @SneakyThrows
    public static void fetchAndValidateDBData() {
        final int maxAttempts = TestConfig.getInt("maxDBValidationAttempts");
        int attempt = 1;
        boolean success = false;
        Throwable lastError = null;
        List<String> allFailures = new ArrayList<>();

        while (attempt <= maxAttempts && !success) {
            LOGGER.info("Fetching device report to perform DB validations (Attempt {}/{})", attempt, maxAttempts);
            clear();

            String deviceReportJson = triggerAzurePipelineToFetchDeviceReport();
            ObjectMapper mapper = new ObjectMapper();
            DeviceReport report = mapper.readValue(deviceReportJson, DeviceReport.class);

            try {
                if (report.getProviders() != null) {
                    for (String providerKey : report.getProviders().keySet()) {
                        DeviceReport.ProviderWrapper wrapper = report.getProviders().get(providerKey);
                        if (wrapper != null && wrapper.getProviders() != null) {
                            DeviceReport.Providers providers = wrapper.getProviders();

                            if (providers.getAcOrderSubscriptionDataProvider() != null) {
                                validateAcOrderSubscriptionDataProvider(providers.getAcOrderSubscriptionDataProvider());
                            } else {
                                softly().fail("Provider " + providerKey + " missing AcOrderSubscriptionDataProvider");
                                LOGGER.error("Provider {} missing AcOrderSubscriptionDataProvider", providerKey);
                            }

                            if (providers.getAssetMongoProvider() != null) {
                                validateAssetMongoProvider(providers.getAssetMongoProvider());
                            } else {
                                softly().fail("Provider " + providerKey + " missing AssetMongoProvider");
                                LOGGER.error("Provider {} missing AssetMongoProvider", providerKey);
                            }

                            if (providers.getDynamoDbProvider() != null) {
                                validateDynamoDbProvider(providers.getDynamoDbProvider());
                            } else {
                                softly().fail("Provider " + providerKey + " missing DynamoDbProvider");
                                LOGGER.error("Provider {} missing DynamoDbProvider", providerKey);
                            }
                        } else {
                            softly().fail("Provider wrapper or inner providers is null for key: " + providerKey);
                            LOGGER.error("Provider wrapper or inner providers is null for key: {}", providerKey);
                        }
                    }
                } else {
                    softly().fail("No providers found in device report.");
                    LOGGER.error("No providers found in device report.");
                }
                assertAll();
                success = true;
            } catch (Throwable e) {
                lastError = e;
                String type = (e instanceof AssertionError) ? "AssertionError" : "Exception";
                allFailures.add("Attempt " + attempt + " failed with " + type + ": " + e.getMessage());
                LOGGER.warn("DB validation failed on attempt {}/{}", attempt, maxAttempts);
                waitForPeriodOf(5);
            }
            attempt++;
        }

        if (!success) {
            StringBuilder sb = new StringBuilder("DB validation failed after " + maxAttempts + " attempts.\n");
            for (String failure : allFailures) {
                sb.append(failure).append("\n");
            }
            throw new AssertionError(sb.toString(), lastError);
        }
    }

    private static void validateAcOrderSubscriptionDataProvider(DeviceReport.AcOrderSubscriptionDataProvider acProvider) {
        DataHolderForDBValidations uiData = TestContextHolder.getDataHolder();

        if (acProvider.getPlans() != null && !acProvider.getPlans().isEmpty()) {
            List<DeviceReport.Plan> validPlans = acProvider.getPlans().stream()
                    .filter(plan -> plan.getPlan() != null &&
                            plan.getPlan().getServiceName() != null &&
                            (plan.getPlan().getServiceName().equalsIgnoreCase("hp_care_pack") ||
                                    plan.getPlan().getServiceName().equalsIgnoreCase("hp_remotecare")))
                    .collect(Collectors.toList());

            for (DeviceReport.Plan plan : validPlans) {
                String planName = plan.getPlan().getDisplayName();
                LOGGER.info("Resource Entitlement DB Validations for : {}", planName);

                if (plan.getStartDate() == null || plan.getEndDate() == null) {
                    if(planName.equalsIgnoreCase("Hp Care Pack 2")) {
                        softly().fail(String.format("%s has null start date or end date which is not acceptable.", planName));
                    }
                    LOGGER.error("{} has null start date or end date", planName);
                    continue;
                } else {
                    String[] formattedPlanDates = getFormattedPlanDates(plan.getStartDate(), plan.getEndDate());
                    String formattedPlanStartDate = formattedPlanDates[0];
                    String formattedPlanEndDate = formattedPlanDates[1];

                    if (plan.getPlan().getServiceName().equalsIgnoreCase("hp_care_pack")) {
                        softly().assertThat(formattedPlanStartDate)
                                .withFailMessage("%s Start Date mismatch! Expected: %s, Actual: %s",
                                        planName, uiData.getAcPlanStartDate(), formattedPlanStartDate)
                                .isEqualTo(uiData.getAcPlanStartDate());
                        LOGGER.info("{} Plan Expected Start Date : {}, Actual Start Date : {}",
                                planName, uiData.getAcPlanStartDate(), formattedPlanStartDate);

                        softly().assertThat(formattedPlanEndDate)
                                .withFailMessage("%s End Date mismatch! Expected: %s, Actual: %s",
                                        planName, uiData.getAcPlanEndDate(), formattedPlanEndDate)
                                .isEqualTo(uiData.getAcPlanEndDate());
                        LOGGER.info("{} Plan Expected End Date : {}, Actual End Date : {}",
                                planName, uiData.getAcPlanEndDate(), formattedPlanEndDate);

                        softly().assertThat(plan.getPlan().getPlanId())
                                .withFailMessage("Plan ID mismatch: expected %s, actual %s",
                                        TestConfig.get("hpActiveCarePlanId"), plan.getPlan().getPlanId())
                                .isEqualTo(TestConfig.get("hpActiveCarePlanId"));
                        LOGGER.info("Expected Plan Id : {}, Actual Plan Id : {}",
                                TestConfig.get("hpActiveCarePlanId"), plan.getPlan().getPlanId());

                        softly().assertThat(plan.getPlan().getServiceName())
                                .withFailMessage("Service Name mismatch: expected %s, actual %s",
                                        TestConfig.get("hpActiveCareServiceName"), plan.getPlan().getServiceName())
                                .isEqualTo(TestConfig.get("hpActiveCareServiceName"));
                        LOGGER.info("Expected Service Name : {}, Actual Service Name : {}",
                                TestConfig.get("hpActiveCareServiceName"), plan.getPlan().getServiceName());
                    } else if (plan.getPlan().getServiceName().equalsIgnoreCase("hp_remotecare")) {
                        softly().assertThat(formattedPlanStartDate)
                                .withFailMessage("%s Start Date mismatch! Expected: %s, Actual: %s",
                                        planName, uiData.getRcPlanStartDate(), formattedPlanStartDate)
                                .isEqualTo(uiData.getRcPlanStartDate());
                        LOGGER.info("{} Plan Expected Start Date : {}, Actual Start Date : {}",
                                planName, uiData.getRcPlanStartDate(), formattedPlanStartDate);

                        softly().assertThat(formattedPlanEndDate)
                                .withFailMessage("%s End Date mismatch! Expected: %s, Actual: %s",
                                        planName, uiData.getRcPlanEndDate(), formattedPlanEndDate)
                                .isEqualTo(uiData.getRcPlanEndDate());
                        LOGGER.info("{} Plan Expected End Date : {}, Actual End Date : {}",
                                planName, uiData.getRcPlanEndDate(), formattedPlanEndDate);

                        softly().assertThat(plan.getPlan().getPlanId())
                                .withFailMessage("Plan ID mismatch: expected %s, actual %s",
                                        TestConfig.get("hpRemoteCarePlanId"), plan.getPlan().getPlanId())
                                .isEqualTo(TestConfig.get("hpRemoteCarePlanId"));
                        LOGGER.info("Expected Plan Id : {}, Actual Plan Id : {}",
                                TestConfig.get("hpRemoteCarePlanId"), plan.getPlan().getPlanId());

                        softly().assertThat(plan.getPlan().getServiceName())
                                .withFailMessage("Service Name mismatch: expected %s, actual %s",
                                        TestConfig.get("hpRemoteCareServiceName"), plan.getPlan().getServiceName())
                                .isEqualTo(TestConfig.get("hpRemoteCareServiceName"));
                        LOGGER.info("Expected Service Name : {}, Actual Service Name : {}",
                                TestConfig.get("hpRemoteCareServiceName"), plan.getPlan().getServiceName());
                    }
                }
                softly().assertThat(plan.getTenantId())
                        .withFailMessage("Tenant ID mismatch: expected %s, actual %s",
                                getTenantId(), plan.getTenantId())
                        .isEqualTo(getTenantId());
                LOGGER.info("Expected Tenant Id : {}, Actual Tenant Id : {}",
                        getTenantId(), plan.getTenantId());
            }
        } else {
            softly().fail("AcOrderSubscriptionDataProvider missing plans.");
            LOGGER.error("AcOrderSubscriptionDataProvider missing plans.");
        }

        String toggle = TestConfig.get("optional-pc-refactor");
        DeviceReport.Subscriptions subscriptions = acProvider.getSubscriptions();

        if (subscriptions == null) {
            softly().fail("AcOrderSubscriptionDataProvider is missing subscriptions.");
            LOGGER.error("AcOrderSubscriptionDataProvider is missing subscriptions.");
            return;
        }

        if ("PCE_DEVICE".equalsIgnoreCase(toggle)) {
            validateDevices(subscriptions.getDevices());
            validateHoldingTanks(subscriptions.getHoldingTanks());
        } else if ("PCE_CAMS_DATA".equalsIgnoreCase(toggle)) {
            validateCamsAndAssetData(subscriptions.getCamsAndAssetData());
        } else if ("PCE_DEVICE_CAMS_DATA".equalsIgnoreCase(toggle)) {
            validateDevices(subscriptions.getDevices());
            if(getCurrentTestType().equals(DEVICE_ENROLLMENT)) {
                validateHoldingTanks(subscriptions.getHoldingTanks());
            }
            validateCamsAndAssetData(subscriptions.getCamsAndAssetData());
        } else {
            softly().fail("Unknown optional-pc-refactor toggle value: " + toggle);
            LOGGER.error("Unknown optional-pc-refactor toggle value: {}", toggle);
        }
    }

    private static void validateDevices(List<DeviceReport.Device> devices) {
        DataHolderForDBValidations uiData = TestContextHolder.getDataHolder();
        if (devices != null && !devices.isEmpty()) {
            LOGGER.info("Validating devices against UI data");
            for (DeviceReport.Device device : devices) {
                String formattedServiceStartDate = formatDate(device.getServiceStartDate(), TimeStampFormat.DB_DATE_FORMAT, TimeStampFormat.UI_DATE_FORMAT_FOR_CSV_DATES);
                String formattedServiceEndDate = formatDate(device.getServiceEndDate(), TimeStampFormat.DB_DATE_FORMAT, TimeStampFormat.UI_DATE_FORMAT_FOR_CSV_DATES);

                softly().assertThat(formattedServiceStartDate)
                        .withFailMessage("Service Start Date mismatch! Expected: %s, Actual: %s",
                                uiData.getFcpkServiceStartDate(), formattedServiceStartDate)
                        .isEqualTo(uiData.getFcpkServiceStartDate());
                LOGGER.info("Device Service Start Date: expected={}, actual={}", uiData.getFcpkServiceStartDate(), formattedServiceStartDate);

                softly().assertThat(formattedServiceEndDate)
                        .withFailMessage("Service End Date mismatch! Expected: %s, Actual: %s",
                                uiData.getFcpkServiceEndDate(), formattedServiceEndDate)
                        .isEqualTo(uiData.getFcpkServiceEndDate());
                LOGGER.info("Device Service End Date: expected={}, actual={}", uiData.getFcpkServiceEndDate(), formattedServiceEndDate);

                softly().assertThat(device.getTenantId())
                        .withFailMessage("Tenant ID mismatch: expected %s, actual %s",
                                getTenantId(), device.getTenantId())
                        .isEqualTo(getTenantId());
                LOGGER.info("Device Tenant ID: expected={}, actual={}", getTenantId(), device.getTenantId());

                String expectedPlanId = getExpectedPlanId(device.getPlanDisplayId());
                softly().assertThat(device.getPlanId())
                        .withFailMessage("Plan ID mismatch: expected %s, actual %s",
                                expectedPlanId, device.getPlanId())
                        .isEqualTo(expectedPlanId);
                LOGGER.info("Device Plan ID: expected={}, actual={}", expectedPlanId, device.getPlanId());

                softly().assertThat(device.getDeviceId())
                        .withFailMessage("Device ID mismatch: expected %s, actual %s",
                                uiData.getDeviceId(), device.getDeviceId())
                        .isEqualTo(uiData.getDeviceId());
                LOGGER.info("Device ID: expected={}, actual={}", uiData.getDeviceId(), device.getDeviceId());
            }
        } else {
            softly().fail("No devices found in subscriptions.");
        }
    }

    private static void validateHoldingTanks(List<DeviceReport.HoldingTank> holdingTanks) {
        DataHolderForDBValidations uiData = TestContextHolder.getDataHolder();
        if (holdingTanks != null && !holdingTanks.isEmpty()) {
            LOGGER.info("Validating holdingTanks against UI data");
            for (DeviceReport.HoldingTank tank : holdingTanks) {
                softly().assertThat(tank.getDeviceSrNo())
                        .withFailMessage("HoldingTank Device Serial Number mismatch! Expected: %s, Actual: %s",
                                getSerialNumber(), tank.getDeviceSrNo())
                        .isEqualTo(getSerialNumber());
                LOGGER.info("HoldingTank Device Serial Number: expected={}, actual={}", getSerialNumber(), tank.getDeviceSrNo());

                softly().assertThat(tank.getTenantName())
                        .withFailMessage("HoldingTank Tenant Name mismatch! Expected: %s, Actual: %s",
                                getTenantName(), tank.getTenantName())
                        .isEqualTo(getTenantName());
                LOGGER.info("HoldingTank Tenant Name: expected={}, actual={}", getTenantName(), tank.getTenantName());

                softly().assertThat(tank.getPlanId())
                        .withFailMessage("HoldingTank Plan ID mismatch! Expected: %s, Actual: %s",
                                getExpectedPlanId(tank.getPlanDisplayId()), tank.getPlanId())
                        .isEqualTo(getExpectedPlanId(tank.getPlanDisplayId()));
                LOGGER.info("HoldingTank Plan ID: expected={}, actual={}", getExpectedPlanId(tank.getPlanDisplayId()), tank.getPlanId());
            }
        } else {
            softly().fail("No holdingTanks found in subscriptions.");
        }
    }

    private static void validateCamsAndAssetData(DeviceReport.CamsAndAssetData camsAndAssetData) {
        DataHolderForDBValidations uiData = TestContextHolder.getDataHolder();
        if (camsAndAssetData == null) {
            softly().fail("No camsAndAssetData found in subscriptions.");
            return;
        }
        if (camsAndAssetData.getCamsData() != null && !camsAndAssetData.getCamsData().isEmpty()) {
            LOGGER.info("Validating cams_data");
            for (DeviceReport.CamsData cams : camsAndAssetData.getCamsData()) {

                String[] formattedServiceDates = getFormattedServiceDates(cams.getServiceStartDate(), cams.getServiceEndDate());
                String formattedServiceStartDate = formattedServiceDates[0];
                String formattedServiceEndDate = formattedServiceDates[1];

                softly().assertThat(cams.getDeviceSrNo())
                        .withFailMessage("CamsData Device Serial Number mismatch! Expected: %s, Actual: %s",
                                getSerialNumber(), cams.getDeviceSrNo())
                        .isEqualTo(getSerialNumber());
                LOGGER.info("CamsData Device Serial Number: expected={}, actual={}", getSerialNumber(), cams.getDeviceSrNo());

                softly().assertThat(cams.getFcpkSerialNumber())
                        .withFailMessage("CamsData Fcpk Serial Number mismatch! Expected: %s, Actual: %s",
                                uiData.getFcpkSerialNumber(), cams.getFcpkSerialNumber())
                        .isEqualTo(uiData.getFcpkSerialNumber());
                LOGGER.info("CamsData Fcpk Serial Number: expected={}, actual={}", uiData.getFcpkSerialNumber(), cams.getFcpkSerialNumber());

                softly().assertThat(formattedServiceStartDate)
                        .withFailMessage("CamsData Fcpk Service Stat Date mismatch! Expected: %s, Actual: %s",
                                uiData.getFcpkServiceStartDate(), formattedServiceStartDate)
                        .isEqualTo(uiData.getFcpkServiceStartDate());
                LOGGER.info("CamsData Service Start Date: expected={}, actual={}", uiData.getFcpkServiceStartDate(), formattedServiceStartDate);

                softly().assertThat(formattedServiceEndDate)
                        .withFailMessage("CamsData Fcpk Service Stat Date mismatch! Expected: %s, Actual: %s",
                                uiData.getFcpkServiceEndDate(), formattedServiceEndDate)
                        .isEqualTo(uiData.getFcpkServiceEndDate());
                LOGGER.info("CamsData Service End Date: expected={}, actual={}", uiData.getFcpkServiceEndDate(), formattedServiceEndDate);

                softly().assertThat(cams.getPlanId())
                        .withFailMessage("CamsData Plan ID mismatch! Expected: %s, Actual: %s",
                                getExpectedPlanId(cams.getPlanDisplayId()), cams.getPlanId())
                        .isEqualTo(getExpectedPlanId(cams.getPlanDisplayId()));
                LOGGER.info("CamsData Plan ID: expected={}, actual={}", getExpectedPlanId(cams.getPlanDisplayId()), cams.getPlanId());

                softly().assertThat(cams.getEndCustomerName())
                        .withFailMessage("CamsData End Customer Name mismatch! Expected: %s, Actual: %s",
                                getTenantName(), cams.getEndCustomerName())
                        .isEqualTo(getTenantName());
                LOGGER.info("CamsData End Customer Name: expected={}, actual={}", getTenantName(), cams.getEndCustomerName());

                softly().assertThat(cams.getPackDescription())
                        .withFailMessage("CamsData Pack Description mismatch! Expected: %s, Actual: %s",
                                uiData.getPackDescription(), cams.getPackDescription())
                        .isEqualTo(uiData.getPackDescription());
                LOGGER.info("CamsData Pack Description: expected={}, actual={}", uiData.getPackDescription(), cams.getPackDescription());
            }
        } else {
            softly().fail("No cams_data found in camsAndAssetData.");
        }
        if (camsAndAssetData.getAssets() != null && !camsAndAssetData.getAssets().isEmpty()) {
            LOGGER.info("Validating assets in camsAndAssetData");
            for (DeviceReport.AssetData asset : camsAndAssetData.getAssets()) {
                softly().assertThat(asset.getTenantName())
                        .withFailMessage("Asset Data : Tenant Name Mismatch! Expected: %s, Actual: %s",
                                getTenantName(), asset.getTenantName())
                        .isEqualTo(getTenantName());
                LOGGER.info("AssetData Tenant Name: expected={}, actual={}", getTenantName(), asset.getTenantName());

                softly().assertThat(asset.getTenantId())
                        .withFailMessage("Asset Data : Tenant ID Mismatch! Expected: %s, Actual: %s",
                                getTenantId(), asset.getTenantId())
                        .isEqualTo(getTenantId());
                LOGGER.info("AssetData Tenant ID: expected={}, actual={}", getTenantId(), asset.getTenantId());

                softly().assertThat(asset.getDeviceId())
                        .withFailMessage("Asset Data : Device ID Mismatch! Expected: %s, Actual: %s",
                                uiData.getDeviceId(), asset.getDeviceId())
                        .isEqualTo(uiData.getDeviceId());
                LOGGER.info("AssetData Device ID: expected={}, actual={}", uiData.getDeviceId(), asset.getDeviceId());
            }
        } else {
            softly().fail("No assets found in camsAndAssetData.");
        }
        if (camsAndAssetData.getReseller() != null && !camsAndAssetData.getReseller().isEmpty()) {
            LOGGER.info("Validating reseller in camsAndAssetData");
            for (DeviceReport.Reseller reseller : camsAndAssetData.getReseller()) {
                softly().assertThat(reseller.getPartnerResellerId())
                        .withFailMessage("Reseller partnerResellerId should not be null")
                        .isNotNull();
                LOGGER.info("Reseller partnerResellerId: actual={}", reseller.getPartnerResellerId());
            }
        } else {
            LOGGER.error("No reseller found in camsAndAssetData.");
        }
    }

    private static void validateAssetMongoProvider(DeviceReport.AssetMongoProvider assetMongoProvider) {
        LOGGER.info("Performing MongoDB Validations");

        softly().assertThat(assetMongoProvider.getTenantId())
                .withFailMessage("Tenant ID mismatch! Expected: %s, Actual: %s",
                        getTenantId(), assetMongoProvider.getTenantId())
                .isEqualTo(getTenantId());
        LOGGER.info("Expected Tenant Id : {}, Actual Tenant Id : {}",
                getTenantId(), assetMongoProvider.getTenantId());

        if (assetMongoProvider.getPlans() != null && assetMongoProvider.getPlans().getEn() != null) {
            DeviceReport.AssetMongoProvider.NameValue plan = assetMongoProvider.getPlans().getEn();

            List<String> expectedPlanNames = getPlanNamesMongodb();
            List<String> expectedPlanValues = getPlanValuesMongodb();
            List<String> actualPlanNames = toList(plan.getName());
            List<String> actualPlanValues = toList(plan.getValue());

            softly().assertThat(actualPlanNames)
                    .withFailMessage("Plan Name mismatch! Expected : %s, Actual: %s", expectedPlanNames, actualPlanNames)
                    .containsExactlyInAnyOrderElementsOf(expectedPlanNames);
            LOGGER.info("Expected Plan Names: {}, Actual Plan Names: {}", expectedPlanNames, actualPlanNames);

            softly().assertThat(actualPlanValues)
                    .withFailMessage("Plan Value mismatch! Expected : %s, Actual: %s", expectedPlanValues, actualPlanValues)
                    .containsExactlyInAnyOrderElementsOf(expectedPlanValues);
            LOGGER.info("Expected Plan Values: {}, Actual Plan Values: {}", expectedPlanValues, actualPlanValues);
        } else {
            softly().fail("AssetMongoProvider missing plans or plans.en object.");
            LOGGER.error("AssetMongoProvider missing plans or plans.en object.");
        }

        if (assetMongoProvider.getStatus() != null && assetMongoProvider.getStatus().getEn() != null) {
            DeviceReport.AssetMongoProvider.NameValue status = assetMongoProvider.getStatus().getEn();
            softly().assertThat(status.getName())
                    .withFailMessage("Status Name mismatch! Expected: %s, Actual: %s",
                            getDeviceStatusNameMongodb(), status.getName())
                    .isEqualTo(getDeviceStatusNameMongodb());
            LOGGER.info("Expected Status Name : {}, Actual Status Name : {}",
                    getDeviceStatusNameMongodb(), status.getName());

            softly().assertThat(status.getValue())
                    .withFailMessage("Status Value mismatch! Expected: %s, Actual: %s",
                            getDeviceStatusValueMongodb(), status.getValue())
                    .isEqualTo(getDeviceStatusValueMongodb());
            LOGGER.info("Expected Status Value : {}, Actual Status Value : {}",
                    getDeviceStatusValueMongodb(), status.getValue());
        } else {
            softly().fail("AssetMongoProvider missing status or status.en object.");
            LOGGER.error("AssetMongoProvider missing status or status.en object.");
        }
    }

    private static void validateDynamoDbProvider(DeviceReport.DynamoDbProvider dynamoDbProvider) {
        DataHolderForDBValidations uiData = TestContextHolder.getDataHolder();
        LOGGER.info("Performing DynamoDB Validations");

        DeviceReport.DynamoDbProvider.DeviceHashDetails deviceHashDetails = dynamoDbProvider.getDeviceHashDetails();

        if (deviceHashDetails != null) {
            softly().assertThat(deviceHashDetails.getDeviceId())
                    .withFailMessage("Device ID mismatch! Expected: %s, Actual: %s",
                            uiData.getDeviceId(), deviceHashDetails.getDeviceId())
                    .isEqualTo(uiData.getDeviceId());
            LOGGER.info("Expected Device ID: {}, Actual Device ID: {}", uiData.getDeviceId(), deviceHashDetails.getDeviceId());

            softly().assertThat(deviceHashDetails.getTenantId())
                    .withFailMessage("Tenant ID mismatch! Expected: %s, Actual: %s",
                            getTenantId(), deviceHashDetails.getTenantId())
                    .isEqualTo(getTenantId());
            LOGGER.info("Expected Tenant ID: {}, Actual Tenant ID: {}", getTenantId(), deviceHashDetails.getTenantId());

            List<String> expectedServiceNames = getServiceNamesDynamodb();
            List<String> actualServiceNames = toList(deviceHashDetails.getServiceName());

            softly().assertThat(actualServiceNames)
                    .withFailMessage("Service Name mismatch! Expected : %s, Actual: %s", expectedServiceNames, actualServiceNames)
                    .containsExactlyInAnyOrderElementsOf(expectedServiceNames);
            LOGGER.info("Expected Service Names: {}, Actual Service Names: {}", expectedServiceNames, actualServiceNames);
        } else {
            softly().fail("DynamoDbProvider missing DeviceHashDetails.");
            LOGGER.error("DynamoDbProvider missing DeviceHashDetails.");
        }
    }

    private static String getExpectedPlanId(String planDisplayId) {
        if (planDisplayId == null || planDisplayId.isEmpty()) {
            throw new IllegalArgumentException("planDisplayId is null or empty");
        }
        if ("ACTIVECARE".equalsIgnoreCase(planDisplayId) || "PREMIUM".equalsIgnoreCase(planDisplayId) || "PREMIUMPLUS".equalsIgnoreCase(planDisplayId)) {
            return TestConfig.get("hpActiveCarePlanId");
        } else if ("OUT_OF_BAND_REMEDIATION".equalsIgnoreCase(planDisplayId)) {
            return TestConfig.get("hpRemoteCarePlanId");
        }
        throw new IllegalArgumentException("Unknown planDisplayId: " + planDisplayId);
    }
}