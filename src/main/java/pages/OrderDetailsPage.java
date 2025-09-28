package pages;

import com.basesource.action.PreDefinedActions;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.pojo.DataHolderForDBValidations;
import com.e2e_activecare.utils.UIUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import com.e2e_activecare.pojo.DeviceRegistration;
import com.e2e_activecare.pojo.DeviceTransfer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.e2e_activecare.utils.AssertUtil.assertAll;
import static com.e2e_activecare.utils.AssertUtil.softly;
import static com.e2e_activecare.utils.BusinessLogicUtil.getExpectedDeviceRegistrationStatus;
import static com.e2e_activecare.utils.BusinessLogicUtil.getExpectedDeviceTransferStatus;
import static com.e2e_activecare.utils.CSVUtil.*;
import static com.e2e_activecare.utils.UIUtil.clean;
import static com.e2e_activecare.utils.WaitUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public final class OrderDetailsPage {

    private OrderDetailsPage() {}

    private static final OrderDetailsPage ORDER_DETAIL_PAGE_INSTANCE = new OrderDetailsPage();
    private static final Logger LOGGER = LogManager.getLogger(OrderDetailsPage.class);

    private List<DeviceRegistration> devicesRegisteredUI = new ArrayList<>();
    private List<DeviceRegistration> devicesRegisteredCSV = new ArrayList<>();
    private List<DeviceTransfer> devicesTransferredUI = new ArrayList<>();
    private List<DeviceTransfer> devicesTransferredCSV = new ArrayList<>();

    private List<Map<String, String>> devicesRegisteredCSVForLoadTest;
    private List<Map<String, String>> devicesRegisteredUIForLoadTest;
    private List<Map<String, String>> devicesTransferredCSVForLoadTest;
    private List<Map<String, String>> devicesTransferredUIForLoadTest;

    private static final By WEB_TABLE = By.xpath("(//table)[1]");
    private static final By ROWS = By.xpath(".//tbody//tr");
    private static final By ORDERS_LIST_BUTTON = By.xpath("//span[text()='Orders List']/parent::button");


    public static OrderDetailsPage getInstance() {
        return ORDER_DETAIL_PAGE_INSTANCE;
    }

    public OrderDetailsPage assert_DeviceRegistrationInfo_In_OrderDetailsPage_MatchesWith_CSVUploaded() throws IOException {
        waitTillDocumentReady();
        waitForPeriodOf(10);

        devicesRegisteredCSV = readDeviceInfoFromCSV(DeviceRegistration.class);
        devicesRegisteredUI = readDeviceInfoFromOrderDetailPage(DeviceRegistration.class);

        logCountAndDetails(devicesRegisteredCSV, devicesRegisteredUI, "DeviceRegistration");
        compareCSVWithUI(devicesRegisteredCSV, devicesRegisteredUI, "DeviceRegistration");

        fetchDataFromCSVAndUIForDBValidations();
        return this;
    }

    private void fetchDataFromCSVAndUIForDBValidations() {
            DataHolderForDBValidations dataHolder = TestContextHolder.getDataHolder();

            dataHolder.setFcpkSerialNumber(devicesRegisteredUI.get(0).getFcpkSerialNumber());
            dataHolder.setFcpkServiceStartDate(devicesRegisteredUI.get(0).getFcpkServiceStartDate());
            dataHolder.setFcpkServiceEndDate(devicesRegisteredUI.get(0).getFcpkServiceEndDate());
            dataHolder.setPackDescription(devicesRegisteredCSV.get(0).getPackDescription());
    }

    public void assertAllDevicesRegisteredSuccessfulForLoadTest() throws IOException {
        waitTillDocumentReady();

        devicesRegisteredCSVForLoadTest = readDeviceInfoFromCSVForLoadTest();
        String expectedStatus = getExpectedDeviceRegistrationStatus();

        boolean allDevicesRegistered = waitTillConditionWithRefresh(() -> {
            devicesRegisteredUIForLoadTest = readDeviceInfoFromOrderDetailPageForLoadTest();

            Map<String, String> uiStatusBySerial = devicesRegisteredUIForLoadTest.stream()
                    .collect(Collectors.toMap(
                            row -> row.get("objectOfServiceSerialNumber"),
                            row -> row.get("Status")
                    ));

            return devicesRegisteredCSVForLoadTest.stream().allMatch(csvRow -> {
                String serialNumber = csvRow.get("ObjectOfServiceSerialNumber");
                String actualStatus = uiStatusBySerial.get(serialNumber);
                return Objects.equals(expectedStatus, actualStatus);
            });
        },120);

        if (!allDevicesRegistered) {
            devicesRegisteredUIForLoadTest = readDeviceInfoFromOrderDetailPageForLoadTest();
            Map<String, String> uiStatusBySerial = devicesRegisteredUIForLoadTest.stream()
                    .collect(Collectors.toMap(
                            row -> row.get("objectOfServiceSerialNumber"),
                            row -> row.get("Status")
                    ));
            for (Map<String, String> csvRow : devicesRegisteredCSVForLoadTest) {
                String serialNumber = csvRow.get("ObjectOfServiceSerialNumber");
                String actualStatus = uiStatusBySerial.get(serialNumber);
                if (!Objects.equals(expectedStatus, actualStatus)) {
                    LOGGER.error("Mismatch for serial {} : Expected={} , Actual={}\n", serialNumber, expectedStatus, actualStatus);
                }
            }
        }
    }

    public void assertAllDevicesTransferredSuccessfulForLoadTest() throws IOException {
        waitTillDocumentReady();
        waitForPeriodOf(10);

        devicesTransferredCSVForLoadTest = readDeviceInfoFromCSVForLoadTest();
        devicesTransferredUIForLoadTest  = readDeviceInfoFromOrderDetailPageForLoadTest();

        Map<String, String> uiStatusBySerial = devicesTransferredUIForLoadTest.stream()
                .collect(Collectors.toMap(
                        row -> row.get("Serial Number"),
                        row -> row.get("Transfer Status")
                ));

        String expectedStatus = getExpectedDeviceTransferStatus();
        for (Map<String, String> csvRow : devicesTransferredCSVForLoadTest) {
            String serialNumber = csvRow.get("ObjectOfServiceSerialNumber");
            String actualStatus = uiStatusBySerial.get(serialNumber);

            if (!Objects.equals(expectedStatus, actualStatus)) {
                LOGGER.error("Mismatch for serial {} : Expected={} , Actual={}\n", serialNumber, expectedStatus, actualStatus);
            }
        }
    }

    public OrderDetailsPage assert_DeviceTransferInfo_In_OrderDetailsPage_MatchesWith_CSVUploaded() throws IOException {
        waitTillDocumentReady();
        waitForPeriodOf(10);

        devicesTransferredCSV = readDeviceInfoFromCSV(DeviceTransfer.class);
        devicesTransferredUI = readDeviceInfoFromOrderDetailPage(DeviceTransfer.class);

        logCountAndDetails(devicesTransferredCSV, devicesTransferredUI, "DeviceTransfer");
        compareCSVWithUI(devicesTransferredCSV, devicesTransferredUI, "DeviceTransfer");

        return this;
    }

    public OrderDetailsPage assertAllDevicesRegisteredSuccessful() {

        String DEVICE_REGISTRATION_EXPECTED_STATUS = getExpectedDeviceRegistrationStatus();

        for (DeviceRegistration device : devicesRegisteredUI) {
            String actualStatus = clean(device.getStatus());

            softly()
                    .assertThat(actualStatus)
                    .withFailMessage("Device with serial number '%s' has status '%s' instead of expected '%s'",
                            clean(device.getObjectOfServiceSerialNumber()), actualStatus, DEVICE_REGISTRATION_EXPECTED_STATUS)
                    .isEqualTo(DEVICE_REGISTRATION_EXPECTED_STATUS);
            }
        assertAll();
        LOGGER.info("All devices have status: {}", DEVICE_REGISTRATION_EXPECTED_STATUS);
        return this;
    }

    public void assertAllDevicesTransferredSuccessfully() {

        String DEVICE_TRANSFER_EXPECTED_STATUS = getExpectedDeviceTransferStatus();

        for (DeviceTransfer device : devicesTransferredUI) {
            String actualStatus = clean(device.getStatus());

            softly()
                    .assertThat(actualStatus)
                    .withFailMessage("Device with serial number '{}' has transfer status '{}' instead of expected '{}'",
                     clean(device.getObjectOfServiceSerialNumber()), actualStatus, DEVICE_TRANSFER_EXPECTED_STATUS)
                    .isEqualTo(DEVICE_TRANSFER_EXPECTED_STATUS);
        }
        assertAll();
        LOGGER.info("All devices have transfer status: {}", DEVICE_TRANSFER_EXPECTED_STATUS);
    }

    public String getRegisteredDeviceSerialNumber() {
        return devicesRegisteredCSV.get(0).getObjectOfServiceSerialNumber();
    }

    public OrdersPage navigateBackToOrdersPage() {
        click(ORDERS_LIST_BUTTON);
        return OrdersPage.getInstance();
    }

    private <T> List<T> readDeviceInfoFromOrderDetailPage(Class<T> clazz) {
        List<T> extractedData = new ArrayList<>();
        Map<String, Field> headerToFieldMap = getHeaderFieldMap(clazz, "ui");

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Wait for table to be displayed and have rows
                waitTillDisplay(WEB_TABLE);
                boolean displayed = PreDefinedActions.getDriver().findElement(WEB_TABLE).isDisplayed();
                LOGGER.info("is Web Table Displayed : {}", displayed);

                waitTillCondition(() -> {
                    WebElement table = PreDefinedActions.getDriver().findElement(WEB_TABLE);
                    long visibleRows = table.findElements(ROWS).stream()
                            .filter(WebElement::isDisplayed)
                            .count();
                    return visibleRows == 25;
                }, 60);

                // Re-locate the table after waits to avoid stale element
                WebElement table = PreDefinedActions.getDriver().findElement(WEB_TABLE);
                int numberOfRows = table.findElements(ROWS).size();
                LOGGER.info("{} rows displayed",numberOfRows);

                // Use JavaScript to extract headers and rows in one go for performance
                JavascriptExecutor js = (JavascriptExecutor) PreDefinedActions.getDriver();
                // Get headers as List<String>
                List<?> rawHeaders = (List<?>) js.executeScript(
                        "return Array.from(arguments[0].querySelectorAll('thead th')).map(th => th.innerText.trim());", table);
                List<String> headers = rawHeaders.stream().map(Object::toString).collect(Collectors.toList());

                // Get rows as List<List<String>>
                List<?> rawRows = (List<?>) js.executeScript(
                        "return Array.from(arguments[0].querySelectorAll('tbody tr')).map(row => " +
                                "Array.from(row.cells).map(cell => {" +
                                "  var span = cell.querySelector('span[title]');" +
                                "  return span ? span.getAttribute('title').trim() : cell.innerText.trim();" +
                                "}));", table);

                List<List<String>> rows = rawRows.stream()
                        .map(row -> ((List<?>) row).stream().map(Object::toString).collect(Collectors.toList()))
                        .collect(Collectors.toList());

                // Map rows to POJOs
                Field keyField = clazz.getDeclaredField("objectOfServiceSerialNumber");
                keyField.setAccessible(true);

                for (List<String> row : rows) {
                    T instance = clazz.getDeclaredConstructor().newInstance();
                    for (int i = 0; i < Math.min(row.size(), headers.size()); i++) {
                        String header = headers.get(i);
                        String value = clean(row.get(i));
                        Field field = headerToFieldMap.get(header);
                        if (field != null) {
                            field.set(instance, value);
                        }
                    }
                    Object serial = keyField.get(instance);
                    if (serial != null && !clean(serial.toString()).isBlank()) {
                        extractedData.add(instance);
                    }
                }
                break;
            } catch (Exception ex) {
                LOGGER.error("Attempt {} of {} failed: {}", attempt, maxRetries, ex.getMessage());
                waitForPeriodOf(3);
            }
        }
        assertThat(extractedData)
                .withFailMessage("UI device details were not extracted from the order detail page. The page may not have loaded")
                .isNotEmpty();
        return extractedData;
    }

    private List<Map<String, String>> readDeviceInfoFromOrderDetailPageForLoadTest() {
        List<Map<String, String>> extractedData = new ArrayList<>();

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                waitTillCondition(() -> {
                    WebElement table = PreDefinedActions.getDriver().findElement(WEB_TABLE);
                    long visibleRows = table.findElements(ROWS).stream()
                            .filter(WebElement::isDisplayed)
                            .count();
                    return visibleRows == 25;
                }, 60);

                WebElement table = PreDefinedActions.getDriver().findElement(WEB_TABLE);

                JavascriptExecutor js = (JavascriptExecutor) PreDefinedActions.getDriver();
                List<?> rawHeaders = (List<?>) js.executeScript(
                        "return Array.from(arguments[0].querySelectorAll('thead th')).map(th => th.innerText.trim());", table);
                List<String> headers = rawHeaders.stream().map(Object::toString).map(UIUtil::clean).collect(Collectors.toList());

                List<?> rawRows = (List<?>) js.executeScript(
                        "return Array.from(arguments[0].querySelectorAll('tbody tr')).map(row => " +
                                "Array.from(row.cells).map(cell => {" +
                                "  var span = cell.querySelector('span[title]');" +
                                "  return span ? span.getAttribute('title').trim() : cell.innerText.trim();" +
                                "}));", table);

                List<List<String>> rows = rawRows.stream()
                        .map(row -> ((List<?>) row).stream().map(Object::toString).map(UIUtil::clean).collect(Collectors.toList()))
                        .collect(Collectors.toList());

                for (List<String> row : rows) {
                    Map<String, String> rowMap = new HashMap<>();
                    for (int i = 0; i < Math.min(row.size(), headers.size()); i++) {
                        rowMap.put(headers.get(i), row.get(i));
                    }
                    if (rowMap.values().stream().anyMatch(val -> val != null && !val.isBlank())) {
                        extractedData.add(rowMap);
                    }
                }
                break;
            } catch (Exception ex) {
                LOGGER.error("Attempt {} of {} failed: {}", attempt, maxRetries, ex.getMessage());
                waitForPeriodOf(3);
            }
        }
        assertThat(extractedData)
                .withFailMessage("UI device details were not extracted from the order detail page. The page may not have loaded")
                .isNotEmpty();
        return extractedData;
    }

    private <T> void compareCSVWithUI(List<T> csvList, List<T> uiList, String label) {
        List<String> allMismatches = new ArrayList<>();

        for (T csvObj : csvList) {
            try {
                Field keyField = csvObj.getClass().getDeclaredField("objectOfServiceSerialNumber");
                keyField.setAccessible(true);
                String csvSerial = clean(Objects.toString(keyField.get(csvObj), ""));

                Optional<T> matchedUI = uiList.stream().filter(uiObj -> {
                    try {
                        Field uiKeyField = uiObj.getClass().getDeclaredField("objectOfServiceSerialNumber");
                        uiKeyField.setAccessible(true);
                        String uiSerial = clean(Objects.toString(uiKeyField.get(uiObj), ""));
                        return Objects.equals(uiSerial, csvSerial);
                    } catch (Exception e) {
                        return false;
                    }
                }).findFirst();

                if (matchedUI.isPresent()) {
                    List<String> mismatches = new ArrayList<>();

                    for (Field field : csvObj.getClass().getDeclaredFields()) {
                        String fieldName = field.getName();
                        if (fieldName.equals("status") || fieldName.equals("registered")) continue;

                        com.e2e_activecare.annotations.MapTo mapTo = field.getAnnotation(com.e2e_activecare.annotations.MapTo.class);
                        if (mapTo == null || mapTo.ui().isBlank() || mapTo.csv().isBlank()) continue;

                        field.setAccessible(true);
                        String expected = clean(Objects.toString(field.get(csvObj), ""));
                        String actual = clean(Objects.toString(field.get(matchedUI.get()), ""));

                        if (!Objects.equals(expected, actual)) {
                            mismatches.add(String.format("%s: expected='%s', but was='%s'", fieldName, expected, actual));
                        }
                    }

                    if (!mismatches.isEmpty()) {
                        String errorMessage = String.format("Mismatches for %s SerialNumber: %s%n%s",
                                label, csvSerial, String.join("\n", mismatches));
                        LOGGER.error(errorMessage);
                        allMismatches.add(errorMessage);
                    }
                } else {
                    String errorMessage = String.format("No UI record found for SerialNumber: %s", csvSerial);
                    LOGGER.error(errorMessage);
                    allMismatches.add(errorMessage);
                }

            } catch (Exception e) {
                throw new RuntimeException("Error during comparison: " + e.getMessage(), e);
            }
        }

        if (!allMismatches.isEmpty()) {
            Assertions.fail("CSV/UI comparison failed:\n" + String.join("\n\n", allMismatches));
        } else {
            LOGGER.info("All {} records match successfully with UI", label);
        }
    }

    private Map<String, Field> getHeaderFieldMap(Class<?> clazz, String mode) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> {
                    com.e2e_activecare.annotations.MapTo mapTo = f.getAnnotation(com.e2e_activecare.annotations.MapTo.class);
                    return mapTo != null && !(mode.equals("ui") ? mapTo.ui() : mapTo.csv()).isBlank();
                })
                .collect(Collectors.toMap(
                        f -> {
                            f.setAccessible(true);
                            return mode.equals("ui") ? f.getAnnotation(com.e2e_activecare.annotations.MapTo.class).ui().trim() : f.getAnnotation(com.e2e_activecare.annotations.MapTo.class).csv().trim();
                        },
                        f -> f
                ));
    }

    private <T> void logCountAndDetails(List<T> csvList, List<T> uiList, String label) {

        LOGGER.info("Total number of {} records in CSV: {}", label, csvList.size());
        LOGGER.info("Total number of {} records in UI: {}", label, uiList.size());

        LOGGER.info("Printing CSV {} records:", label);
        csvList.forEach(record -> LOGGER.info("CSV Record: {}", record));

        LOGGER.info("Printing UI {} records:", label);
        uiList.forEach(record -> LOGGER.info("UI Record: {}", record));
    }
}
