package utils;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.config.TestConfig;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.e2e_activecare.constants.FrameWorkConstants.getPathForJSON_ToFetchDeviceCount_With_NAPlan;
import static com.e2e_activecare.constants.FrameWorkConstants.getPathForJSON_ToFetchDeviceCount_With_NAPlan_ForWxpTenant;
import static com.e2e_activecare.utils.BusinessLogicUtil.getApiUrl;
import static com.e2e_activecare.utils.JSONUtil.readJsonFromFile;
import static io.restassured.http.ContentType.JSON;

public final class APIUtil {

    private APIUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(APIUtil.class);

    private static final ThreadLocal<Integer> CURRENT_ITERATION = new ThreadLocal<>();

    public static void setCurrentIteration(int iteration) {
        CURRENT_ITERATION.set(iteration);
    }

    public static int getCurrentIteration() {
        return CURRENT_ITERATION.get() != null ? CURRENT_ITERATION.get() : 0;
    }

    public static void clearCurrentIteration() {
        CURRENT_ITERATION.remove();
    }

    @SneakyThrows
    public static int fetchCountOfDevicesWith_NAPlan() {

        String requestJsonFilePath = getPathForJSON_ToFetchDeviceCount_With_NAPlan();
        JsonNode requestBodyJson = readJsonFromFile(requestJsonFilePath);

        String authToken = new PreDefinedActions().getTokenFromLocalStorage("dui_token_e");
        String endpoint = getApiUrl();

        Response response = RestAssured.given()
                .proxy("web-proxy.corp.hp.com", 8080)
                .header("Authorization", "Bearer " + authToken)
                .contentType(JSON)
                .body(requestBodyJson)
                .log()
                .ifValidationFails()
                .post(endpoint);

        printSerialNumbersWith_NA_Plan_IfExists(response);
        return response.jsonPath().getList("records").size();
    }

    @SneakyThrows
    public static int fetchCountOfDevicesWith_NAPlan_ForWxpTenant() {

        String requestJsonFilePath = getPathForJSON_ToFetchDeviceCount_With_NAPlan_ForWxpTenant();
        JsonNode requestBodyJson = readJsonFromFile(requestJsonFilePath);

        String authToken = new PreDefinedActions().getTokenFromLocalStorage("dui_token_e");
        String endpoint = TestConfig.get("assetSearchUrlWxp");

        Response response = RestAssured.given()
                .proxy("web-proxy.corp.hp.com", 8080)
                .header("Authorization", "Bearer " + authToken)
                .contentType(JSON)
                .body(requestBodyJson)
                .log()
                .ifValidationFails()
                .post(endpoint);

        printSerialNumbersWith_NA_Plan_IfExists(response);
        return response.jsonPath().getList("records").size();
    }

    private static void printSerialNumbersWith_NA_Plan_IfExists(Response response) {
        List<String> serialNumbers = response.jsonPath().getList("records.serialNumber");
        if (!serialNumbers.isEmpty()) {
            LOGGER.info("ITERATION {}: Below are {} serial numbers assigned N/A plan:",
                    getCurrentIteration(), serialNumbers.size());
            serialNumbers.forEach(serialNumber ->
                    LOGGER.info("  Run {}: {}", getCurrentIteration(), serialNumber)
            );
        } else {
            LOGGER.info("Iteration {}: No N/A plans found", getCurrentIteration());
        }
    }
}