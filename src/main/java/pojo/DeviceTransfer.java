package pojo;

import com.e2e_activecare.annotations.MapTo;
import com.e2e_activecare.pojo.stratagies.RegionStrategy;
import com.e2e_activecare.pojo.stratagies.SerialNumberForDeviceTransfer;
import com.e2e_activecare.pojo.stratagies.TenantDisplayIdStrategy;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;
import uk.co.jemos.podam.common.PodamStrategyValue;

@Data
public class DeviceTransfer {

    @PodamStrategyValue(SerialNumberForDeviceTransfer.class)
    @MapTo(csv = "ObjectOfServiceSerialNumber", ui = "Serial Number")
    private String objectOfServiceSerialNumber;

    @PodamStrategyValue(TenantDisplayIdStrategy.class)
    @MapTo(csv = "TenantDisplayId", ui = "Destination Tenant Display ID")
    private String tenantDisplayId;

    @PodamStrategyValue(RegionStrategy.class)
    @MapTo(csv = "Region", ui = "")
    private String region;

    @PodamExclude
    @MapTo(csv = "", ui = "Transfer Status")
    private String status;
}
