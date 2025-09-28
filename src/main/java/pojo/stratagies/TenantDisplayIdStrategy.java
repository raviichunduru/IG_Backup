package pojo.stratagies;

import com.e2e_activecare.enums.Environment;
import com.e2e_activecare.enums.TestType;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.e2e_activecare.context.EnvironmentHolder.getEnvironment;
import static com.e2e_activecare.context.TestContextHolder.getCurrentTestType;

public class TenantDisplayIdStrategy implements AttributeStrategy<String> {

    @Override
    public String getValue(Class<?> attrType, List<Annotation> annotations) {

        Environment environment = getEnvironment();
        TestType currentTestType = getCurrentTestType();

        switch (environment) {
            case US_STABLE:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "C-6MhD7cVu";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "C-6H8hfGsG";
                    case PARTNER_DEVICETRANSFER_AC_TO_ACPI:
                        return "C-Ki4YDp2e";
                    case PARTNER_DEVICETRANSFER_ACPI_TO_AC:
                        return "C-M8kfKp3C";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "C-aG5g2XiB";
                }
            case EU_STABLE:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "C-aj4mWR7U";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "C-8pB8ytSA";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "C-4Vdw2iYG";
                }
            case US_STAGING:
                return "C-58NKwngR";
            case EU_STAGING:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                    return "C-Sr8yF7xR";
                case ASSET_UPLOAD_PERF_CUJ:
                    return "C-4rgxT7CC";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                    return "C-RVy5whA7";
            }
            default:
                throw new IllegalStateException("Unexpected environment: " + environment);
        }
    }
}