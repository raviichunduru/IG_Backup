package utils;

import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.OrderType;
import com.e2e_activecare.pojo.DeviceRegistration;
import com.e2e_activecare.pojo.DeviceTransfer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.e2e_activecare.constants.FrameWorkConstants.getCsvToUploadDir;
import static com.e2e_activecare.constants.FrameWorkConstants.getSerialNumbersFilePath;
import static com.e2e_activecare.enums.TimeStampFormat.YYYY_MM_DD_HH_MM_SS_SSS;
import static com.e2e_activecare.utils.BusinessLogicUtil.getNumberOfDevices;
import static com.e2e_activecare.utils.DataUtil.generateDeviceData;
import static com.e2e_activecare.utils.DateUtil.getCurrentTimestamp;
import static com.e2e_activecare.utils.FileUtil.getUploadedFileName;
import static com.e2e_activecare.utils.ShellUtil.getCsvForDeviceEnroll;
import static com.e2e_activecare.utils.ShellUtil.getCsvForDeviceUnroll;
import static org.assertj.core.api.Assertions.assertThat;

public final class CSVUtil {

    private CSVUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(CSVUtil.class);

    public static <T> File generateCSVFile(OrderType orderType, Class<T> clazz) throws IOException {
        List<T> dataList = generateDeviceData(clazz);
        List<Field> mappedFields = getMappedFields(clazz);
        File file = createCSVFile(orderType);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeHeader(writer, mappedFields);
            writeRecords(writer, dataList, mappedFields);
        }
        LOGGER.info("CSV file successfully written with {} records for type: {}", dataList.size(), clazz.getSimpleName());
        return file;
    }

    public static <T> List<T> readDeviceInfoFromCSV(Class<T> clazz) throws IOException {
        List<String> lines = readCSVLines();
        if (lines.isEmpty()) return Collections.emptyList();

        String[] headers = lines.get(0).split(",", -1);
        Map<String, Field> headerFieldMap = buildHeaderFieldMap(clazz);

        return parseRecords(lines, headers, headerFieldMap, clazz);
    }

    public static List<Map<String, String>> readDeviceInfoFromCSVForLoadTest() throws IOException {
        List<String> lines = readCSVLines();
        if (lines.isEmpty()) return Collections.emptyList();

        String[] headers = lines.get(0).split(",", -1);
        // Clean headers to handle invisible Unicode and whitespace
        String[] cleanedHeaders = Arrays.stream(headers)
                .map(UIUtil::clean)
                .toArray(String[]::new);

        // Reading only the first 25 data lines (skipping header)
        List<Map<String, String>> extractedData = lines.stream()
                .skip(1)
                .limit(25)
                .map(line -> {
                    String[] values = line.split(",", -1);
                    Map<String, String> row = new HashMap<>();
                    IntStream.range(0, Math.min(cleanedHeaders.length, values.length))
                            .forEach(i -> row.put(cleanedHeaders[i], UIUtil.clean(values[i])));
                    return row;
                })
                .collect(Collectors.toList());

        assertThat(extractedData)
                .as("CSV device details should not be empty. Check the CSV file and data format.")
                .isNotEmpty();

        return extractedData;
    }

    private static <T> List<Field> getMappedFields(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(com.e2e_activecare.annotations.MapTo.class))
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    private static File createCSVFile(OrderType orderType) {
        String fileName = orderType.getBaseName() + "_" + getCurrentTimestamp(YYYY_MM_DD_HH_MM_SS_SSS) + ".csv";
        File dir = new File(getCsvToUploadDir());
        return new File(dir, fileName);
    }

    private static void writeHeader(BufferedWriter writer, List<Field> fields) throws IOException {
        String header = fields.stream()
                .map(f -> f.getAnnotation(com.e2e_activecare.annotations.MapTo.class).csv().trim())
                .collect(Collectors.joining(","));
        writer.write(header);
        writer.newLine();
    }

    private static <T> void writeRecords(BufferedWriter writer, List<T> dataList, List<Field> fields) throws IOException {
        for (T obj : dataList) {
            String record = fields.stream()
                    .map(f -> getFieldValueSafe(f, obj))
                    .collect(Collectors.joining(","));
            writer.write(record);
            writer.newLine();
        }
    }

    private static String getFieldValueSafe(Field field, Object obj) {
        try {
            Object value = field.get(obj);
            return value == null ? "" : value.toString().trim();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> readCSVLines() throws IOException {
        File file = new File(getCsvToUploadDir(), getUploadedFileName());
        return Files.readAllLines(file.toPath());
    }

    private static <T> Map<String, Field> buildHeaderFieldMap(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(com.e2e_activecare.annotations.MapTo.class))
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toMap(
                        f -> f.getAnnotation(com.e2e_activecare.annotations.MapTo.class).csv().trim(),
                        Function.identity()));
    }

    private static <T> List<T> parseRecords(List<String> lines, String[] headers,
                                            Map<String, Field> fieldMap, Class<T> clazz) {
        return lines.stream().skip(1).map(line -> {
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                String[] values = line.split(",", -1);
                IntStream.range(0, Math.min(headers.length, values.length)).forEach(i -> {
                    Field field = fieldMap.get(headers[i].trim());
                    if (field != null) {
                        try {
                            field.set(instance, values[i].trim());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse line: " + line, e);
            }
        }).collect(Collectors.toList());
    }

    public static void generateLoadTestCSVs() throws IOException {

        int numRows = getNumberOfDevices();

        String deviceImportCsvFilePath = getCsvToUploadDir() + "/LoadTest_DeviceImport.csv";
        String deviceTransferCsvFilePath = getCsvToUploadDir() + "/LoadTest_DeviceTransfer.csv";
        String deviceDeleteCsvFilePath = getCsvToUploadDir() + "/LoadTest_DeviceDelete.csv";
        String assetUploadCsvFilePath = getCsvToUploadDir() + "/AssetUpload.csv";
        String deviceEnrollCsvFilePath = getCsvForDeviceEnroll();
        String deviceUnrollCsvFilePath = getCsvForDeviceUnroll();

        List<String> serialNumbers = Files.readAllLines(getSerialNumbersFilePath());

        LOGGER.info("Number of serial numbers before generating CSV: {}", serialNumbers.size());

        if (serialNumbers.size() < numRows) {
            throw new RuntimeException("Not enough serial numbers in the file for the specified number of rows.");
        }

        List<String> serialNumbersToUse = serialNumbers.subList(0, numRows);

        generateDeviceImportCsv(deviceImportCsvFilePath, serialNumbersToUse);
        generateDeviceTransferCsv(deviceTransferCsvFilePath, serialNumbersToUse);
        generateDeviceDeleteCsv(deviceDeleteCsvFilePath, serialNumbersToUse);
        generateAssetUploadCsv(assetUploadCsvFilePath, serialNumbersToUse);
        generateDeviceEnrollCsv(deviceEnrollCsvFilePath, serialNumbersToUse);
        generateDeviceUnrollCsv(deviceUnrollCsvFilePath, serialNumbersToUse);

        if(!TestConfig.getBoolean("reUseSameSerialNumbers") && !(TestContextHolder.getCurrentOrderTypes().length > 1)) {
            serialNumbers.subList(0, numRows).clear();
            Files.write(getSerialNumbersFilePath(), serialNumbers);
        }

        LOGGER.info("Number of serial numbers after generating CSV: {}", serialNumbers.size());
        LOGGER.info("CSV files generated successfully.");
    }

    private static void generateDeviceImportCsv(String filePath, List<String> serialNumbers) throws IOException {
        PodamFactory factory = new PodamFactoryImpl();
        List<DeviceRegistration> registrations = new ArrayList<>();
        for (String serialNumber : serialNumbers) {
            DeviceRegistration deviceRegistration = factory.manufacturePojo(DeviceRegistration.class);
            deviceRegistration.setObjectOfServiceSerialNumber(serialNumber);
            registrations.add(deviceRegistration);
        }

        try (FileWriter writer = new FileWriter(filePath, false)) {
            String header = String.join(",",
                    "FCPKSerialNumber", "FCPKServiceStartDate", "FCPKServiceEndDate", "FCPKProductID",
                    "ObjectOfServiceSerialNumber", "EndCustomerName", "EndCustomerPrimaryEmail",
                    "EndCustomerAddressLine1", "EndCustomerAddressLine2", "EndCustomerAddressLine3",
                    "EndCustomerCity", "EndCustomerStateProvince", "EndCustomerPostalCode",
                    "EndCustomerCountryCode", "EndCustomerCountryName", "Registered"
            ) + ",";
            writer.write(header + "\n");

            for (DeviceRegistration registration : registrations) {
                String row = String.join(",",
                        registration.getFcpkSerialNumber(),
                        registration.getFcpkServiceStartDate(),
                        registration.getFcpkServiceEndDate(),
                        registration.getFcpkProductID(),
                        registration.getObjectOfServiceSerialNumber(),
                        registration.getEndCustomerName(),
                        registration.getEndCustomerPrimaryEmail(),
                        registration.getEndCustomerAddressLine1(),
                        registration.getEndCustomerAddressLine2(),
                        registration.getEndCustomerAddressLine3(),
                        registration.getEndCustomerCity(),
                        registration.getEndCustomerStateProvince(),
                        registration.getEndCustomerPostalCode(),
                        registration.getEndCustomerCountryCode(),
                        registration.getEndCustomerCountryName(),
                        registration.getRegistered()
                ) + ",";
                writer.write(row + "\n");
            }
        }
    }

    private static void generateDeviceTransferCsv(String filePath, List<String> serialNumbers) throws IOException {
        PodamFactory factory = new PodamFactoryImpl();
        List<DeviceTransfer> transfers = new ArrayList<>();
        for (String serialNumber : serialNumbers) {
            DeviceTransfer deviceTransfer = factory.manufacturePojo(DeviceTransfer.class);
            deviceTransfer.setObjectOfServiceSerialNumber(serialNumber);
            transfers.add(deviceTransfer);
        }

        try (FileWriter writer = new FileWriter(filePath, false)) {
            String header = String.join(",", "ObjectOfServiceSerialNumber", "TenantDisplayId", "Region") + ",";
            writer.write(header + "\n");

            for (DeviceTransfer transfer : transfers) {
                String row = String.join(",",
                        transfer.getObjectOfServiceSerialNumber(),
                        transfer.getTenantDisplayId(),
                        transfer.getRegion()
                ) + ",";
                writer.write(row + "\n");
            }
        }
    }

    private static void generateDeviceDeleteCsv(String filePath, List<String> serialNumbers) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, false)) {
            String header = "Serial Number (mandatory),";
            writer.write(header + "\n");

            for (String serialNumber : serialNumbers) {
                writer.write(serialNumber + ",\n");
            }
        }
    }

    private static void generateAssetUploadCsv(String filePath, List<String> serialNumbers) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, false)) {
            String header = "Serial Number (mandatory),";
            writer.write(header + "\n");

            for (String serialNumber : serialNumbers) {
                writer.write(serialNumber + ",\n");
            }
        }
    }

    private static void generateDeviceEnrollCsv(String filePath, List<String> serialNumbers) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, false)) {
            for (String serialNumber : serialNumbers) {
                writer.write(serialNumber + "\n");
            }
        }
    }

    private static void generateDeviceUnrollCsv(String filePath, List<String> serialNumbers) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, false)) {
            String header = "Serial Number (mandatory)";
            writer.write(header + "\n");

            for (String serialNumber : serialNumbers) {
                writer.write(serialNumber + "\n");
            }
        }
    }
}
