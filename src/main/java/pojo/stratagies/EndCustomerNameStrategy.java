package pojo.stratagies;

import com.e2e_activecare.enums.Environment;
import com.e2e_activecare.enums.TestType;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.e2e_activecare.context.EnvironmentHolder.getEnvironment;
import static com.e2e_activecare.context.TestContextHolder.getCurrentTestType;

public class EndCustomerNameStrategy implements AttributeStrategy<String> {
    @Override
    public String getValue(Class<?> attrType, List<Annotation> annotations) {

        Environment environment = getEnvironment();
        TestType currentTestType = getCurrentTestType();

        switch (environment) {
            case US_STABLE:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "CUJTenantUSSTABLE02";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "CUJTenantUSSTABLE03";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "CUJTenantUSSTABLE01";
                }
            case EU_STABLE:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "tenant02eustablecuj";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "tenant03eustablecuj";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "tenant01eustablecuj";
                }
            case US_STAGING:
                return "DoNotDeleteActiveCareOnlyTenant";
            case EU_STAGING:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "donotdeleteacpiusereu";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "donotdeleteacpiwpteu1";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "DoNotDeleteActiveCareOnlyTenantEU";
                }
            default:
                throw new IllegalStateException("Unexpected environment: " + environment);
        }
    }
}