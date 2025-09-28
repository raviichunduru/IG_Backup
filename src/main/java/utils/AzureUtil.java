package utils;

import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.context.EnvironmentHolder;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.pojo.context.SerialNumberHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.e2e_activecare.context.EnvironmentHolder.*;
import static com.e2e_activecare.pojo.context.SerialNumberHolder.getSerialNumber;
import static com.e2e_activecare.utils.BusinessLogicUtil.getTenantId;

public final class AzureUtil {

    private AzureUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(AzureUtil.class);

    private static final String CRON_JOB_END_POINT_TO_PROCESS_ORDERS = TestConfig.get("cronJobEndPointToProcessOrders");
    private static final String CRON_JOB_END_POINT_TO_MAKE_SNR_ENTRY = TestConfig.get("cronJobEndPointToMakeSNREntry");
    private static final String CRON_JOB_END_POINT_TO_DELETE_SNR_ENTRY = TestConfig.get("cronJobEndPointToDeleteSNREntry");

    private static final String BASE_URL = "https://dev.azure.com";
    private static final String ORGANIZATION = "hpcodeway";
    private static final String PROJECT = "hpac";
    private static final int PIPELINE_ID = getPipeLineID();
    private static final String API_VERSION = "7.1-preview.1";

    private static final int MAX_ATTEMPTS = 30;
    private static final int POLL_INTERVAL_MS = 30_000;

    private static String AUTH_HEADER = "";

    public static void triggerAzurePipelineToProcessOrder() {
        try {
            String apiEndpoint = getHost() + CRON_JOB_END_POINT_TO_PROCESS_ORDERS;
            String triggerUrl = buildTriggerUrl();
            String requestBody = buildRequestBody(apiEndpoint, getEnvironmentName());

            Response triggerResponse = RestAssured.given()
                    .header("Authorization", getAuthHeader())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .when()
                    .post(triggerUrl)
                    .then()
                    .statusCode(200)
                    .log().ifValidationFails()
                    .extract().response();

            int runId = triggerResponse.jsonPath().getInt("id");
            LOGGER.info("Pipeline triggered to process order. Run ID: {}", runId);

            pollPipelineStatus(runId);

        } catch (Exception e) {
            LOGGER.error("Exception occurred while triggering Azure pipeline: ", e);
        }
    }

    public static void triggerAzurePipelineToMakeSNREntry(String serialNo) {

        try {
            String apiEndpoint = "--request POST '" + getHost() + CRON_JOB_END_POINT_TO_MAKE_SNR_ENTRY + "'"
                    + " -H 'content-type: application/json' "
                    + "-d '{"
                    + "\\\"serialNo\\\":\\\"" + serialNo + "\\\","
                    + "\\\"product_no\\\":\\\"1134466#BQQQ\\\","
                    + "\\\"bios_uuid\\\":\\\"66BA178E-7D82-4BB6-9SSS-9335956F8521\\\","
                    + "\\\"deviceid_loc\\\":\\\"\\\","
                    + "\\\"deviceid_non_loc\\\":\\\"\\\","
                    + "\\\"dual_snr_status\\\":\\\"\\\""
                    + "}'";
            String triggerUrl = buildTriggerUrl();
            String requestBody = buildRequestBody(apiEndpoint, getEnvironmentName());

            Response triggerResponse = RestAssured.given()
                    .header("Authorization", getAuthHeader())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .when()
                    .post(triggerUrl)
                    .then()
                    .statusCode(200)
                    .log().ifValidationFails()
                    .extract().response();

            int runId = triggerResponse.jsonPath().getInt("id");
            LOGGER.info("Pipeline triggered to make SNR entry for {}. Run ID: {}", serialNo, runId);

            pollPipelineStatus(runId);

        } catch (Exception e) {
            LOGGER.error("Exception occurred while triggering Azure pipeline: ", e);
        }
    }

    public static void triggerAzurePipelineToDeleteSNREntry(String serialNo) {
        try {
            String apiEndpoint = "--request DELETE '" + getHost() + CRON_JOB_END_POINT_TO_DELETE_SNR_ENTRY + serialNo + "'";
            String triggerUrl = buildTriggerUrl();
            String requestBody = buildRequestBody(apiEndpoint, getEnvironmentName());

            Response triggerResponse = RestAssured.given()
                    .header("Authorization", getAuthHeader())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .when()
                    .post(triggerUrl)
                    .then()
                    .statusCode(200)
                    .log().ifValidationFails()
                    .extract().response();

            int runId = triggerResponse.jsonPath().getInt("id");
            LOGGER.info("Pipeline triggered to DELETE SNR entry for {}. Run ID: {}", serialNo, runId);

            pollPipelineStatus(runId);

        } catch (Exception e) {
            LOGGER.error("Exception occurred while triggering Azure pipeline for DELETE: ", e);
        }
    }

    public static void deleteDevice() {
        String tenantId = getTenantId();
        String deviceId = TestContextHolder.getDataHolder().getDeviceId();

        try {
            String apiEndpoint = "curl -i --request POST " + getHost() + "/services/drs/internal/1.0/tenants/" + tenantId + "/devices/bulk-delete " +
                    "--header 'X-HPTM-Transaction-ID: delete-device001' " +
                    "--header 'Content-Type: application/json' " +
                    "--data-raw '{\\\"resources\\\":[\\\"" + deviceId + "\\\"]}'";

            String triggerUrl = buildTriggerUrl();
            String requestBody = buildRequestBody(apiEndpoint, getEnvironmentName());

            Response triggerResponse = RestAssured.given()
                    .header("Authorization", getAuthHeader())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .when()
                    .post(triggerUrl)
                    .then()
                    .statusCode(200)
                    .log().ifValidationFails()
                    .extract().response();

            int runId = triggerResponse.jsonPath().getInt("id");
            LOGGER.info("Pipeline triggered to delete device {}. Run ID: {}", getSerialNumber(), runId);

            pollPipelineStatus(runId);

        } catch (Exception e) {
            LOGGER.error("Exception occurred while triggering Azure pipeline to delete device: ", e);
        }
    }

    public static String triggerAzurePipelineToFetchDeviceReport() {
        String serialNumber = getSerialNumber();
        String deviceReportJson;

        int maxAttemptsForDeviceReport = 3;

        for (int attempt = 1; attempt <= maxAttemptsForDeviceReport; attempt++) {
            try {
                String environmentName = getEnvironmentName();

                String endpointUrl = TestConfig.get("deviceReportEndPoint");
                String apiEndpoint = "-X POST " + endpointUrl
                        + " -H 'Content-Type: application/json' "
                        + "-d '{"
                        + "\\\"serialNumber\\\": \\\"" + serialNumber + "\\\","
                        + " \\\"providersObj\\\": []"
                        + "}'";

                String triggerUrl = buildTriggerUrl();
                String requestBody = buildRequestBody(apiEndpoint, environmentName);

                Response triggerResponse = RestAssured.given()
                        .header("Authorization", getAuthHeader())
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .when()
                        .post(triggerUrl)
                        .then()
                        .statusCode(200)
                        .log().ifValidationFails()
                        .extract().response();

                int runId = triggerResponse.jsonPath().getInt("id");
                LOGGER.info("Pipeline triggered to fetch device report for {}. Run ID: {}", serialNumber, runId);

                pollPipelineStatus(runId);

                String logUrl = String.format(
                        "https://dev.azure.com/%s/%s/_apis/build/builds/%d/logs/6",
                        ORGANIZATION, PROJECT, runId
                );

                Response logResponse = RestAssured.given()
                        .header("Authorization", getAuthHeader())
                        .when()
                        .get(logUrl)
                        .then()
                        .statusCode(200)
                        .extract().response();

                String logContent = logResponse.getBody().asString();
                deviceReportJson = extractDeviceReportJson(logContent);

                if (deviceReportJson != null) {
                    LOGGER.info("Extracted device report JSON on attempt {}: {}", attempt, deviceReportJson);
                    return deviceReportJson;
                } else {
                    LOGGER.warn("Attempt {}: Device report JSON not found in log.", attempt);
                    if (attempt < maxAttemptsForDeviceReport) {
                        Thread.sleep(5000);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Exception during attempt {} to fetch device report: ", attempt, e);
                if (attempt < maxAttemptsForDeviceReport) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        LOGGER.error("Sleep interrupted: ", ie);
                    }
                }
            }
        }
        throw new RuntimeException("No data returned for the device report after "
                + maxAttemptsForDeviceReport + " attempts.");
    }

    /**
     Extracts and returns a valid JSON fragment from the provided log content.

     This method handles cases where the JSON object is split across multiple lines
     and may include invalid or non-printable characters.

     It works by iterating over each character of the log content. When the first '{'
     is encountered, it begins accumulating characters into a StringBuilder. The method
     tracks the number of opening and closing braces to ensure that a balanced JSON object
     is assembled.

     Once a balanced JSON fragment is detected (i.e. when the brace count returns to zero),
     the candidate JSON is cleaned by removing non-printable characters. It then tries to
     parse the cleaned candidate using Jackson's ObjectMapper. If the parsed JSON contains the
     required keys ("htmlPath", "jsonPath", and "message"), the method returns the candidate JSON.

     If no valid JSON fragment is found in the log, the method logs a warning and returns null.
     */
    private static String extractDeviceReportJson(String logContent) {
        String result = null;
        if (logContent != null && !logContent.isEmpty()) {
            StringBuilder jsonBuilder = new StringBuilder();
            boolean jsonStarted = false;
            int braceCount = 0;
            for (char ch : logContent.toCharArray()) {
                if (ch == '{') {
                    if (!jsonStarted) {
                        jsonStarted = true;
                    }
                    braceCount++;
                }
                if (jsonStarted) {
                    jsonBuilder.append(ch);
                }
                if (ch == '}') {
                    braceCount--;
                    if (jsonStarted && braceCount == 0) {
                        String candidate = jsonBuilder.toString().trim();
                        candidate = candidate.replaceAll("[^\\x20-\\x7E]", "");
                        try {
                            JsonNode jsonNode = new ObjectMapper().readTree(candidate);
                            if (jsonNode.has("htmlPath") && jsonNode.has("jsonPath") && jsonNode.has("message")) {
                                result = candidate;
                                break;
                            }
                        } catch (Exception ignored) {

                        }
                        jsonBuilder.setLength(0);
                        jsonStarted = false;
                    }
                }
            }
        }
        return result;
    }

    private static void pollPipelineStatus(int runId) {
        String pollUrl = buildPollUrl(runId);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);

                Response pollResponse = RestAssured.given()
                        .header("Authorization", getAuthHeader())
                        .get(pollUrl)
                        .then()
                        .statusCode(200)
                        .extract().response();

                String state = pollResponse.jsonPath().getString("state");
                String result = pollResponse.jsonPath().getString("result");

                LOGGER.info("Attempt {} - State: {}, Result: {}", attempt, state, result);

                if ("completed".equalsIgnoreCase(state)) {
                    if ("succeeded".equalsIgnoreCase(result)) {
                        LOGGER.info("Pipeline completed successfully");
                    } else {
                        LOGGER.warn("Pipeline completed with result: {}", result);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                LOGGER.error("Polling interrupted", e);
                return;
            } catch (Exception e) {
                LOGGER.error("Error during polling attempt {}", attempt, e);
            }
        }
        LOGGER.warn("Pipeline did not complete within the expected time");
    }

    private static String buildTriggerUrl() {
        return String.format("%s/%s/%s/_apis/pipelines/%d/runs?api-version=%s",
                BASE_URL, ORGANIZATION, PROJECT, PIPELINE_ID, API_VERSION);
    }

    private static String buildPollUrl(int runId) {
        return String.format("%s/%s/%s/_apis/pipelines/%d/runs/%d?api-version=%s",
                BASE_URL, ORGANIZATION, PROJECT, PIPELINE_ID, runId, API_VERSION);
    }

    private static String buildRequestBody(String apiEndpoint, String environment) {
        return "{\n" +
                "  \"resources\": {\n" +
                "    \"repositories\": {\n" +
                "      \"self\": {\n" +
                "        \"refName\": \"refs/heads/master\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"templateParameters\": {\n" +
                "    \"api_endpoint\": \"" + apiEndpoint + "\",\n" +
                "    \"environment\": \"" + environment + "\"\n" +
                "  }\n" +
                "}";
    }

    private static String getAuthHeader() {
        if (AUTH_HEADER.isEmpty()) {
            String pat = TestConfig.get("PAT");
            String auth = ":" + pat;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            AUTH_HEADER = "Basic " + encoded;
        }
        return AUTH_HEADER;
    }

    public static int getPipeLineID() {
        String rawEnvironmentName = EnvironmentHolder.getEnvironment().getValue();
        switch (rawEnvironmentName) {
            case "US-Stable":
            case "EU-Stable":
                return 32487;
            case "EU-Staging":
            case "US-Staging":
                return 32498;
            default:
                throw new IllegalArgumentException("Unsupported environment: " + rawEnvironmentName);

        }
    }
}