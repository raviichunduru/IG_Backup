package utils;

import com.e2e_activecare.annotations.SkipSNRDeletion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

import static com.e2e_activecare.pojo.context.SerialNumberHolder.getSerialNumber;
import static com.e2e_activecare.utils.AzureUtil.triggerAzurePipelineToDeleteSNREntry;
import static com.e2e_activecare.context.TestContextHolder.getCurrentTestMethod;

public final class DataCleanupUtil {

    private DataCleanupUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(DataCleanupUtil.class);

    public static void performSNRDeletion() {
        if (shouldSkipSNRDeletion()) {
            return;
        }

        String serialNumber = getSerialNumber();
        if (serialNumber == null || serialNumber.isEmpty()) {
            LOGGER.info("No serial number found for SNR deletion.");
            return;
        }

        LOGGER.info("Performing SNR deletion for serial number: {}", serialNumber);
        triggerAzurePipelineToDeleteSNREntry(serialNumber);
    }

    private static boolean shouldSkipSNRDeletion() {
        Method method = getCurrentTestMethod();
        if (method.isAnnotationPresent(SkipSNRDeletion.class)) {
            LOGGER.info("Skipping SNR deletion for test: {}", method.getName());
            return true;
        }
        return false;
    }
}