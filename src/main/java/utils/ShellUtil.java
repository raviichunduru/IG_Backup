package utils;

import com.e2e_activecare.pojo.DeviceEnrollmentInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static com.e2e_activecare.utils.BusinessLogicUtil.getNumberOfDevices;
import static com.e2e_activecare.utils.BusinessLogicUtil.isRunningTestPerfCUJ;
import static com.e2e_activecare.utils.PerformanceMetricUtil.endTimer;
import static com.e2e_activecare.utils.PerformanceMetricUtil.startTimer;
import static org.assertj.core.api.Assertions.assertThat;

public final class ShellUtil {

    private ShellUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(ShellUtil.class);

    private static final String BASE_DIR_FOR_MULTI_ENROLLMENT = "src/test/java/resources/multienrollment/";
    private static final String SERIAL_CSV_PATH = BASE_DIR_FOR_MULTI_ENROLLMENT + "serial_numbers.csv";
    private static final String INPUT_JSON_PATH = BASE_DIR_FOR_MULTI_ENROLLMENT + "input.json";
    private static final String ENROLLMENT_METRICS_CSV_PATH = BASE_DIR_FOR_MULTI_ENROLLMENT + "enrolmentMetrics.csv";
    private static final String DEVICE_UNROLL_CSV_PATH = Paths.get(BASE_DIR_FOR_MULTI_ENROLLMENT + "deviceUnroll.csv").toAbsolutePath().toString();

    private static final Integer numberOfDevices = getNumberOfDevices();

    @SneakyThrows
    public static void runDeviceEnrollFlow(List<String> serialNumbers) {
        updateSerialNumbersCsv(serialNumbers);
        writeDeviceEnrollmentInputJson();
        executeShellScriptToPerformDeviceEnrollment();
        assertAllSuccessInCsv(serialNumbers.size());
    }

    @SneakyThrows
    public static void runDeviceEnrollFlowForLoadTest() {
        writeDeviceEnrollmentInputJson();
        executeShellScriptToPerformDeviceEnrollment();
        assertAllSuccessInCsv(numberOfDevices);
    }

    @SneakyThrows
    private static void updateSerialNumbersCsv(List<String> serialNumbers) {
        Files.write(Paths.get(SERIAL_CSV_PATH), serialNumbers);
        LOGGER.info("Updated serial_numbers.csv with {} serials", serialNumbers.size());
    }

    public static String getCsvForDeviceEnroll() {
        return SERIAL_CSV_PATH;
    }

    public static String getCsvForDeviceUnroll() {
        return DEVICE_UNROLL_CSV_PATH;
    }

    @SneakyThrows
    public static void writeDeviceEnrollmentInputJson() {
        PodamFactory factory = new PodamFactoryImpl();
        DeviceEnrollmentInput input = factory.manufacturePojo(DeviceEnrollmentInput.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(INPUT_JSON_PATH), input);
    }

    @SneakyThrows
    private static void executeShellScriptToPerformDeviceEnrollment() {
        if(isRunningTestPerfCUJ()) {
            startTimer("cpin_enrollment");
        }
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        List<String> command;
        if (isWindows) {
            String gitBashPath = findGitBashPath();
            if (gitBashPath == null) {
                throw new IllegalStateException("Git Bash not found. Please install Git Bash or ensure it is in the PATH.");
            }
            command = Arrays.asList(gitBashPath, "-c",
                    "export HTTP_PROXY=http://web-proxy.corp.hp.com:8080 && " +
                            "export HTTPS_PROXY=http://web-proxy.corp.hp.com:8080 && " +
                            "./run.sh");
        } else {
            command = Arrays.asList("/bin/bash", "-c",
                    "export HTTP_PROXY=http://web-proxy.corp.hp.com:8080 && " +
                            "export HTTPS_PROXY=http://web-proxy.corp.hp.com:8080 && " +
                            "./run.sh");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(BASE_DIR_FOR_MULTI_ENROLLMENT));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }
        }

        int exitCode = process.waitFor();
        LOGGER.info("Shell script finished with exit code: {}", exitCode);

        if (exitCode != 0) {
            throw new IllegalStateException("Shell script execution failed with exit code: " + exitCode);
        } else {
            if(isRunningTestPerfCUJ()){
                endTimer("cpin_enrollment");
                startTimer("ac_plan_allocation_in_wxp");
            }
        }
    }

    private static String findGitBashPath() {
        String[] possiblePaths = {
                "C:\\Program Files\\Git\\bin\\bash.exe",
                "C:\\Program Files (x86)\\Git\\bin\\bash.exe",
                System.getenv("ProgramFiles") + "\\Git\\bin\\bash.exe",
                System.getenv("ProgramFiles(x86)") + "\\Git\\bin\\bash.exe",
                System.getenv("LOCALAPPDATA") + "\\Programs\\Git\\bin\\bash.exe"
        };

        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                return path;
            }
        }

        try {
            Process process = new ProcessBuilder("bash", "--version").start();
            if (process.waitFor() == 0) {
                return "bash";
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SneakyThrows
    private static void assertAllSuccessInCsv(int expectedCount) {
        try (Reader reader = Files.newBufferedReader(Paths.get(ENROLLMENT_METRICS_CSV_PATH));
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> allRows = csvReader.readAll();
            if (allRows.size() <= 1) {
                throw new IllegalStateException("CSV has no data rows.");
            }

            int successCount = getSuccessCount(allRows);

            assertThat(successCount)
                    .withFailMessage(String.format("Expected %d successful enrollments, but got %d", expectedCount, successCount))
                    .isEqualTo(expectedCount);

            LOGGER.info("All {} enrollments are successful.", expectedCount);
        }
    }

    private static int getSuccessCount(List<String[]> allRows) {
        String[] header = allRows.get(0);
        int successIndex = -1;

        for (int i = 0; i < header.length; i++) {
            if ("Success".equalsIgnoreCase(header[i].trim())) {
                successIndex = i;
                break;
            }
        }

        if (successIndex == -1) {
            throw new IllegalStateException("'Success' column not found in enrollmentMetrics.csv.");
        }

        int successCount = 0;
        for (int i = 1; i < allRows.size(); i++) {
            String[] row = allRows.get(i);
            if (successIndex < row.length && "true".equalsIgnoreCase(row[successIndex].trim())) {
                successCount++;
            }
        }
        return successCount;
    }
}