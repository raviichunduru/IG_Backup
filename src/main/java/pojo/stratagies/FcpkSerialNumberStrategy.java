package pojo.stratagies;

import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.e2e_activecare.utils.RandomUtil.getRandomNumeric;

public class FcpkSerialNumberStrategy implements AttributeStrategy<String> {
    @Override
    public String getValue(Class<?> aClass, List<Annotation> list) {
        String newSerial = "FCPK3400" + getRandomNumeric(4);
        return newSerial;
    }
}