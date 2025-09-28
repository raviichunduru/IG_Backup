package pojo.stratagies;

import uk.co.jemos.podam.common.AttributeStrategy;
import com.e2e_activecare.config.TestConfig;

import java.lang.annotation.Annotation;
import java.util.List;

public class CPINStrategy implements AttributeStrategy<String> {
    @Override
    public String getValue(Class<?> attrType, List<Annotation> annotations) {
        return TestConfig.get("cpin");
    }
}