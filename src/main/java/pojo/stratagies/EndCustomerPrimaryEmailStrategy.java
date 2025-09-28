package pojo.stratagies;

import com.e2e_activecare.config.TestConfig;
import com.e2e_activecare.context.TestContextHolder;
import com.e2e_activecare.enums.Environment;
import com.e2e_activecare.context.EnvironmentHolder;
import com.e2e_activecare.enums.TestType;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

public class EndCustomerPrimaryEmailStrategy implements AttributeStrategy<String> {

    @Override
    public String getValue(Class<?> attrType, List<Annotation> annotations) {

        Environment environment = EnvironmentHolder.getEnvironment();
        TestType currentTestType = TestContextHolder.getCurrentTestType();

        switch (environment) {
            case US_STABLE:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "cujtenantusstable02@yopmail.com";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "cujtenantusstable03@yopmail.com";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "cujtenantusstable01@yopmail.com";
                }
            case EU_STABLE:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "tenant02eustablecuj@yopmail.com";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "tenant03eustablecuj@yopmail.com";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "tenant01eustablecuj@yopmail.com";
                }
            case US_STAGING:
                return "donotdeleteactivecareonlyuserus@workforceqa.ext.hp.com";
            case EU_STAGING:
                switch (currentTestType) {
                    case DEVICE_ENROLLMENT_PERF_CUJ:
                        return "donotdeleteacpiusereu@workforceqa.ext.hp.com";
                    case ASSET_UPLOAD_PERF_CUJ:
                        return "donotdeleteacpiwpteu1@workforceqa.ext.hp.com";
                    case DEVICE_IMPORT_TRANSFER_PERF_CUJ:
                    default:
                        return "donotdeleteactivecareonlyusereu@workforceqa.ext.hp.com";
                }
            default:
                throw new IllegalStateException("Unexpected environment: " + environment);
        }
    }
}