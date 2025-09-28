package pojo;

import com.e2e_activecare.pojo.stratagies.CPINStrategy;
import com.e2e_activecare.pojo.stratagies.EnvironmentStrategy;
import lombok.Data;
import uk.co.jemos.podam.common.PodamStrategyValue;
import uk.co.jemos.podam.common.PodamStringValue;

@Data
public class DeviceEnrollmentInput {
    @PodamStrategyValue(EnvironmentStrategy.class)
    private String environment;

    @PodamStringValue(strValue = "eu")
    private String region;

    @PodamStringValue(strValue = "CPIN")
    private String enroll_type;

    @PodamStrategyValue(CPINStrategy.class)
    private String pin;
}