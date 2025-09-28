package pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataHolderForDBValidations {
    private String fcpkSerialNumber;
    private String fcpkServiceStartDate;
    private String fcpkServiceEndDate;
    private String packDescription;
    private String deviceId;
    private String acPlanStartDate;
    private String acPlanEndDate;
    private String rcPlanStartDate;
    private String rcPlanEndDate;
}