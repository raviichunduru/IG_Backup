package pojo.stratagies;

import com.e2e_activecare.pojo.context.SerialNumberHolder;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

public class SerialNumberForPartnerDeviceTransfer implements AttributeStrategy<String> {
    @Override
    public String getValue(Class<?> aClass, List<Annotation> list) {
        return SerialNumberHolder.getSerialNumber();
    }
}
