package utils;

import com.e2e_activecare.enums.OrderType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.e2e_activecare.constants.FrameWorkConstants.getCsvToUploadDir;
import static com.e2e_activecare.utils.CSVUtil.generateCSVFile;

@Slf4j
public final class FileUtil {

    private FileUtil() {}

    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);
    private static final ThreadLocal<List<String>> uploadedFileNames = ThreadLocal.withInitial(ArrayList::new);

    @SneakyThrows
    public static <T> File getCSVFileForUpload(OrderType orderType, Class<T> clazz) {
        File csvFile = generateCSVFile(orderType, clazz);

        Optional.of(csvFile.getName()).ifPresent(fileName -> uploadedFileNames.get().add(fileName));
        return csvFile;
    }

    public static String getUploadedFileName() {
        List<String> files = uploadedFileNames.get();
        return files.get(files.size() - 1);
    }

    public static void clearUploadedFiles() {
        List<String> files = uploadedFileNames.get();
        for (String fileName : files) {
            Path path = Path.of(getCsvToUploadDir(), fileName);
            try {
                boolean deleted = Files.deleteIfExists(path);
                if (!deleted) {
                    LOGGER.warn("Failed to delete temp file: {}", path.toAbsolutePath());
                }
            } catch (Exception e) {
                LOGGER.error("Error deleting temp file: {}\nType: {}\nMessage: {}",
                        path.toAbsolutePath(), e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        uploadedFileNames.remove();
        LOGGER.info("temporary csv files generated are cleared from listener");
    }

    @SneakyThrows
    public static String confirmIfFileExists(String filePath) {
        File file = new File(filePath);
        String canonicalPath = file.getCanonicalPath();
        LOGGER.info("Uploading file from: {}", canonicalPath);
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + canonicalPath);
        }
        return canonicalPath;
    }

    public static void setFileNameForLoadTest(String filePath) {
        uploadedFileNames.get().add(new File(filePath).getName());
    }
}