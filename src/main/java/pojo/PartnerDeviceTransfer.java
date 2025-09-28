package pojo;

import com.e2e_activecare.annotations.MapTo;
import com.e2e_activecare.pojo.stratagies.SerialNumberForPartnerDeviceTransfer;
import com.e2e_activecare.pojo.stratagies.TenantDisplayIdStrategy;
import lombok.Data;
import uk.co.jemos.podam.common.PodamStrategyValue;
import uk.co.jemos.podam.common.PodamStringValue;

@Data
public class PartnerDeviceTransfer {

    @PodamStrategyValue(SerialNumberForPartnerDeviceTransfer.class)
    @MapTo(csv = "SerialNumber", ui = "Serial Number")
    private String serialNumber;

    @PodamStrategyValue(TenantDisplayIdStrategy.class)
    @MapTo(csv = "TenantDisplayId", ui = "Tenant Display ID")
    private String tenantDisplayId;
}
