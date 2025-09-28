package pojo.stratagies;

import com.e2e_activecare.pojo.context.SerialNumberHolder;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.e2e_activecare.utils.RandomUtil.getRandomAlphaNumeric;

public class SerialNumberForDeviceRegistration implements AttributeStrategy<String> {
    @Override
    public String getValue(Class<?> aClass, List<Annotation> list) {
        String existingSerial = SerialNumberHolder.getSerialNumber();
        if (existingSerial != null && !existingSerial.isEmpty()) {
            return existingSerial;
        }
        String newSerial = "5CG139" + getRandomAlphaNumeric(4);
        SerialNumberHolder.setSerialNumber(newSerial);
        return newSerial;
    }
}