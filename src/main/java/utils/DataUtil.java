package utils;

import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.pojo.DeviceRegistration;
import com.e2e_activecare.pojo.DeviceTransfer;
import com.e2e_activecare.pojo.PartnerDeviceTransfer;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.e2e_activecare.context.TestContextHolder.getCsvUploadCount;
import static com.e2e_activecare.utils.AzureUtil.triggerAzurePipelineToMakeSNREntry;
import static com.e2e_activecare.utils.BusinessLogicUtil.shouldSkipSNRInsert;

public final class DataUtil {

    private DataUtil() {}
    private static final PodamFactory FACTORY = new PodamFactoryImpl();

    public static <T> List<T> generateDeviceData(Class<T> clazz) {
        int deviceCount = getCountForClass(clazz);
        List<T> deviceList = new ArrayList<>();
        Set<String> triggeredSerialNumbers = new HashSet<>();

        for (int i = 0; i < deviceCount; i++) {
            T device = generateSingleData(clazz);

            if (clazz == DeviceRegistration.class && !shouldSkipSNRInsert() && getCsvUploadCount() < 1) {
                String serialNumber = ((DeviceRegistration) device).getObjectOfServiceSerialNumber();
                if (serialNumber != null && triggeredSerialNumbers.add(serialNumber)) {
                    triggerAzurePipelineToMakeSNREntry(serialNumber);
                }
            }
            deviceList.add(device);
        }
        return deviceList;
    }

    private static <T> T generateSingleData(Class<T> clazz) {
        return FACTORY.manufacturePojo(clazz);
    }

    private static <T> int getCountForClass(Class<T> clazz) {
        if (clazz == DeviceRegistration.class) {
            return TestConfig.getInt("numberOfDevicesForFunctionalCUJs");
        } else if (clazz == DeviceTransfer.class) {
            return TestConfig.getInt("numberOfDevicesForFunctionalCUJs");
        }
        else if (clazz == PartnerDeviceTransfer.class) {
            return TestConfig.getInt("numberOfDevicesForFunctionalCUJs");
        }
        throw new IllegalArgumentException("Unsupported class type: " + clazz.getSimpleName());
    }
}
