package pojo;

import com.e2e_activecare.annotations.MapTo;
import com.e2e_activecare.pojo.stratagies.*;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;
import uk.co.jemos.podam.common.PodamStrategyValue;
import uk.co.jemos.podam.common.PodamStringValue;

@Data
public class DeviceRegistration {

    @PodamStrategyValue(FcpkSerialNumberStrategy.class)
    @MapTo(csv = "FCPKSerialNumber", ui = "fcpkSerialNumber")
    private String fcpkSerialNumber;

    @PodamStringValue(strValue = "1-Jan-22")
    @MapTo(csv = "FCPKServiceStartDate", ui = "fcpkServiceStartDate")
    private String fcpkServiceStartDate;

    @PodamStringValue(strValue = "1-Jan-35")
    @MapTo(csv = "FCPKServiceEndDate", ui = "fcpkServiceEndDate")
    private String fcpkServiceEndDate;

    @PodamStringValue(strValue = "FCPKProdUpdate005")
    @MapTo(csv = "FCPKProductID", ui = "fcpkProductID")
    private String fcpkProductID;

    @PodamStrategyValue(SerialNumberForDeviceRegistration.class)
    @MapTo(csv = "ObjectOfServiceSerialNumber", ui = "objectOfServiceSerialNumber")
    private String objectOfServiceSerialNumber;

    @PodamStrategyValue(EndCustomerNameStrategy.class)
    @MapTo(csv = "EndCustomerName", ui = "endCustomerName")
    private String endCustomerName;

    @PodamStrategyValue(EndCustomerPrimaryEmailStrategy.class)
    @MapTo(csv = "EndCustomerPrimaryEmail", ui = "endCustomerPrimaryEmail")
    private String endCustomerPrimaryEmail;

    @PodamStringValue(strValue = "Ravet")
    @MapTo(csv = "EndCustomerAddressLine1", ui = "endCustomerAddressLine1")
    private String endCustomerAddressLine1;

    @PodamStringValue(strValue = "Akurdi")
    @MapTo(csv = "EndCustomerAddressLine2", ui = "endCustomerAddressLine2")
    private String endCustomerAddressLine2;

    @PodamStringValue(strValue = "India")
    @MapTo(csv = "EndCustomerAddressLine3", ui = "endCustomerAddressLine3")
    private String endCustomerAddressLine3;

    @PodamStringValue(strValue = "Pune")
    @MapTo(csv = "EndCustomerCity", ui = "endCustomerCity")
    private String endCustomerCity;

    @PodamStringValue(strValue = "Maharashtra")
    @MapTo(csv = "EndCustomerStateProvince", ui = "endCustomerStateProvince")
    private String endCustomerStateProvince;

    @PodamStringValue(strValue = "412101")
    @MapTo(csv = "EndCustomerPostalCode", ui = "endCustomerPostalCode")
    private String endCustomerPostalCode;

    @PodamStrategyValue(CountryStrategy.class)
    @MapTo(csv = "EndCustomerCountryCode", ui = "endCustomerCountryCode")
    private String endCustomerCountryCode;

    @PodamStringValue(strValue = "India")
    @MapTo(csv = "EndCustomerCountryName", ui = "endCustomerCountryName")
    private String endCustomerCountryName;

    @PodamStringValue(strValue = "1")
    @MapTo(csv = "Registered", ui = "Registered")
    private String registered;

    @PodamStringValue(strValue = "HP Pack Description")
    @MapTo(csv = "PackDescription", ui = "")
    private String packDescription;

    @PodamExclude
    @MapTo(csv = "", ui = "Status")
    private String status;
}
