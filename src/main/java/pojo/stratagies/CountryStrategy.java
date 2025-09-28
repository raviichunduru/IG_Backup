package pojo.stratagies;

import com.e2e_activecare.context.EnvironmentHolder;
import com.e2e_activecare.enums.Environment;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

public class CountryStrategy implements AttributeStrategy<String> {

    @Override
    public String getValue(Class<?> attrType, List<Annotation> annotations) {

        Environment environment = EnvironmentHolder.getEnvironment();
        switch (environment) {
            case US_STAGING:
            case US_STABLE:
                return "US";
            case EU_STAGING:
            case EU_STABLE:
                return "FR";
            default:
                throw new IllegalStateException("Unexpected environment: " + environment);
        }
    }
}