package pojo.stratagies;

import uk.co.jemos.podam.common.AttributeStrategy;
import com.e2e_activecare.context.EnvironmentHolder;
import com.e2e_activecare.enums.Environment;

import java.lang.annotation.Annotation;
import java.util.List;

public class EnvironmentStrategy implements AttributeStrategy<String> {
    @Override
    public String getValue(Class<?> attrType, List<Annotation> annotations) {
        Environment environment = EnvironmentHolder.getEnvironment();
        switch (environment) {
            case US_STAGING:
            case EU_STAGING:
                return "stage";
            case US_STABLE:
            case EU_STABLE:
                return "dev";
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }
}