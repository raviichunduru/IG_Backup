package constants;

import com.e2e_activecare.enums.TestType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.e2e_activecare.context.TestContextHolder.getCurrentTestType;
import static com.e2e_activecare.enums.TimeStampFormat.YYYY_MM_DD_HH_MM_SS;
import static com.e2e_activecare.utils.DateUtil.getCurrentTimestamp;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class FrameWorkConstants {

    private FrameWorkConstants() {}

    private static final String PROJECT_PATH = System.getProperty("user.dir");
    private static final String RESOURCE_PATH = Paths.get(PROJECT_PATH, "src", "test","java", "resources").toString();
    private static final String E2E_USSTAGING_CONFIG_FILE_PATH = Paths.get(PROJECT_PATH, "credentials", "E2E-USStaging.properties").toString();
    private static final String E2E_EUSTAGING_CONFIG_FILE_PATH = Paths.get(PROJECT_PATH, "credentials", "E2E-EUStaging.properties").toString();
    private static final String E2E_USSTABLE_CONFIG_FILE_PATH = Paths.get(PROJECT_PATH, "credentials", "E2E-USStable.properties").toString();
    private static final String E2E_EUSTABLE_CONFIG_FILE_PATH = Paths.get(PROJECT_PATH, "credentials", "E2E-EUStable.properties").toString();

    private static final String CSV_TO_UPLOAD_DIR = Paths.get(RESOURCE_PATH, "csvtoupload_func").toString();
    private static final String CSV_TO_UPLOAD_DIR_FOR_DEVICE_IMPORT = Paths.get(RESOURCE_PATH, "csvfordeviceimport_perf").toString();
    private static final String CSV_TO_UPLOAD_DIR_FOR_DEVICE_ENROLLMENT = Paths.get(RESOURCE_PATH, "csvfordevicenrollment_perf").toString();
    private static final String CSV_TO_UPLOAD_DIR_FOR_ASSET_UPLOAD = Paths.get(RESOURCE_PATH, "csvforassetupload_perf").toString();
    private static final String CSV_TO_DELETE_DEVICES_FOR_DEVICE_IMPORT = Paths.get(CSV_TO_UPLOAD_DIR_FOR_DEVICE_IMPORT, "LoadTest_DeviceDelete.csv").toString();
    private static final String CSV_TO_DELETE_DEVICES_FOR_DEVICE_ENROLLMENT = Paths.get(CSV_TO_UPLOAD_DIR_FOR_DEVICE_ENROLLMENT, "LoadTest_DeviceDelete.csv").toString();
    private static final String CSV_TO_DELETE_DEVICES_FOR_ASSET_UPLOAD = Paths.get(CSV_TO_UPLOAD_DIR_FOR_ASSET_UPLOAD, "LoadTest_DeviceDelete.csv").toString();
    private static final String CSV_TO_ADD_DEVICES = Paths.get(CSV_TO_UPLOAD_DIR, "AssetUpload.csv").toString();
    private static final String ORIGINAL_FILE_NAME_FOR_DEVICEIMPORT_LOADTEST = "LoadTest_DeviceImport.csv";
    private static final String ORIGINAL_FILE_NAME_FOR_DEVICETRANSFER_LOADTEST = "LoadTest_DeviceTransfer.csv";

    private static final Path SERIAL_NUMBERS_FILE_PATH_FOR_DEVICE_IMPORT = Paths.get(CSV_TO_UPLOAD_DIR_FOR_DEVICE_IMPORT, "SerialNumbers.csv");
    private static final Path SERIAL_NUMBERS_FILE_PATH_FOR_DEVICE_ENROLLMENT = Paths.get(CSV_TO_UPLOAD_DIR_FOR_DEVICE_ENROLLMENT, "SerialNumbers.csv");
    private static final Path SERIAL_NUMBERS_FILE_PATH_ASSET_UPLOAD = Paths.get(CSV_TO_UPLOAD_DIR_FOR_ASSET_UPLOAD, "SerialNumbers.csv");

    private static final String JSON_TO_FETCH_DEVICE_COUNT_WITH_NA_PLAN = Paths.get(RESOURCE_PATH, "requestjsons", "jsonToFetchDeviceCountWith_NA_Plan").toString();
    private static final String JSON_TO_FETCH_DEVICE_COUNT_WITH_NA_PLAN_FOR_WXP_TENANT = Paths.get(RESOURCE_PATH, "requestjsons", "jsonToFetchDeviceCountWith_NA_Plan_For_WxpTenant").toString();
    private static final String JSON_TO_MAKE_IOT_API_CALL_FOR_DEVICE_ENROLLMENT = Paths.get(RESOURCE_PATH, "requestjsons", "jsonForIOTEnrollment").toString();

    public static String getCsvToUploadDir() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                return CSV_TO_UPLOAD_DIR_FOR_DEVICE_IMPORT;
            case DEVICE_ENROLLMENT_PERF_CUJ:
                return CSV_TO_UPLOAD_DIR_FOR_DEVICE_ENROLLMENT;
            case ASSET_UPLOAD_PERF_CUJ:
                return CSV_TO_UPLOAD_DIR_FOR_ASSET_UPLOAD;
            case DEVICE_IMPORT_TRANSFER:
            case DEVICE_ENROLLMENT:
            case PARTNER_DEVICETRANSFER_AC_TO_ACPI:
            case PARTNER_DEVICETRANSFER_ACPI_TO_AC:
                return CSV_TO_UPLOAD_DIR;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String getCsvToDeleteDevicesAsMSP() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                return CSV_TO_DELETE_DEVICES_FOR_DEVICE_IMPORT;
            case DEVICE_ENROLLMENT_PERF_CUJ:
                return CSV_TO_DELETE_DEVICES_FOR_DEVICE_ENROLLMENT;
            case ASSET_UPLOAD_PERF_CUJ:
                return CSV_TO_DELETE_DEVICES_FOR_ASSET_UPLOAD;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String getCsvToAddDevicesAsMSP() {
        return CSV_TO_ADD_DEVICES;
    }

    public static String getE2eUSStagingConfigFilePath() {
        return E2E_USSTAGING_CONFIG_FILE_PATH;
    }

    public static String getE2eEUStagingConfigFilePath() {
        return E2E_EUSTAGING_CONFIG_FILE_PATH;
    }

    public static String getE2eUSStableConfigFilePath() {
        return E2E_USSTABLE_CONFIG_FILE_PATH;
    }

    public static String getE2eEUStableConfigFilePath() {
        return E2E_EUSTABLE_CONFIG_FILE_PATH;
    }

    public static String getLoadTest_DeviceImport_CsvPath() {
        String timestamp = getCurrentTimestamp(YYYY_MM_DD_HH_MM_SS);
        String newFileName = "LoadTest_DeviceImport_" + timestamp + ".csv";
        Path source = Paths.get(getCSVDirectoryBasedOnTestType(), ORIGINAL_FILE_NAME_FOR_DEVICEIMPORT_LOADTEST);
        Path target = Paths.get(getCSVDirectoryBasedOnTestType(), newFileName);

        try {
            Files.copy(source, target, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy file for upload: " + e.getMessage(), e);
        }
        return target.toString();
    }

    private static String getCSVDirectoryBasedOnTestType() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                return CSV_TO_UPLOAD_DIR_FOR_DEVICE_IMPORT;
            case DEVICE_ENROLLMENT_PERF_CUJ:
                return CSV_TO_UPLOAD_DIR_FOR_DEVICE_ENROLLMENT;
            case ASSET_UPLOAD_PERF_CUJ:
                return CSV_TO_UPLOAD_DIR_FOR_ASSET_UPLOAD;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String getLoadTest_DeviceTransfer_CsvPath() {
        String timestamp = getCurrentTimestamp(YYYY_MM_DD_HH_MM_SS);
        String newFileName = "LoadTest_DeviceTransfer_" + timestamp + ".csv";
        Path source = Paths.get(getCSVDirectoryBasedOnTestType(), ORIGINAL_FILE_NAME_FOR_DEVICETRANSFER_LOADTEST);
        Path target = Paths.get(getCSVDirectoryBasedOnTestType(), newFileName);

        try {
            Files.copy(source, target, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy file for upload: " + e.getMessage(), e);
        }
        return target.toString();
    }

    public static Path getSerialNumbersFilePath() {
        TestType testType = getCurrentTestType();
        switch (testType) {
            case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                return SERIAL_NUMBERS_FILE_PATH_FOR_DEVICE_IMPORT;
            case DEVICE_ENROLLMENT_PERF_CUJ:
                return SERIAL_NUMBERS_FILE_PATH_FOR_DEVICE_ENROLLMENT;
            case ASSET_UPLOAD_PERF_CUJ:
                return SERIAL_NUMBERS_FILE_PATH_ASSET_UPLOAD;
            default:
                throw new IllegalStateException("Unexpected test type: " + testType);
        }
    }

    public static String getPathForJSON_ToFetchDeviceCount_With_NAPlan() {
        return JSON_TO_FETCH_DEVICE_COUNT_WITH_NA_PLAN;
    }

    public static String getPathForJSON_ToFetchDeviceCount_With_NAPlan_ForWxpTenant() {
        return JSON_TO_FETCH_DEVICE_COUNT_WITH_NA_PLAN_FOR_WXP_TENANT;
    }

    public static String getPathForJSON_ToMakeIOTAPICall_ForDeviceEnrollment() {
        return JSON_TO_MAKE_IOT_API_CALL_FOR_DEVICE_ENROLLMENT;
    }
}