package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DeviceReport {

    @JsonProperty("Providers")
    private Map<String, ProviderWrapper> providers;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ProviderWrapper {
        @JsonProperty("Providers")
        private Providers providers;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Providers {
        @JsonProperty("AcOrderSubscriptionDataProvider")
        private AcOrderSubscriptionDataProvider acOrderSubscriptionDataProvider;

        @JsonProperty("AssetMongoProvider")
        private AssetMongoProvider assetMongoProvider;

        @JsonProperty("DynamoDbProvider")
        private DynamoDbProvider dynamoDbProvider;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class AcOrderSubscriptionDataProvider {
        private List<Object> activeOrders;
        private List<Plan> plans;
        private Subscriptions subscriptions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Plan {
        @JsonProperty("resourceid")
        private String resourceId;
        @JsonProperty("updatedby")
        private String updatedBy;
        @JsonProperty("orderid")
        private String orderId;
        @JsonProperty("startdate")
        private String startDate;
        @JsonProperty("resourcetype")
        private String resourceType;
        @JsonProperty("version")
        private String version;
        @JsonProperty("enddate")
        private String endDate;
        @JsonProperty("appsite")
        private String appSite;
        @JsonProperty("subscriptionstate")
        private String subscriptionState;
        @JsonProperty("createdby")
        private String createdBy;
        @JsonProperty("assetid")
        private String assetId;
        @JsonProperty("tenantid")
        private String tenantId;
        @JsonProperty("plan")
        private PlanDetails plan;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class PlanDetails {
        @JsonProperty("application_name")
        private String applicationName;
        @JsonProperty("service_name")
        private String serviceName;
        private String name;
        private String description;
        private String planId;
        @JsonProperty("display_name")
        private String displayName;
        @JsonProperty("app_site")
        private String appSite;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Subscriptions {
        // For PCE_DEVICE and PCE_DEVICE_CAMS_DATA
        private List<HoldingTank> holdingTanks;
        private List<Device> devices;

        // For PCE_CAMS_DATA and PCE_DEVICE_CAMS_DATA
        private CamsAndAssetData camsAndAssetData;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class HoldingTank {
        private String lastDtAttempted;
        private String endCustomerPrimaryEmail;
        private String endCustomerStateProvince;
        private String created_at;
        private String registered;
        private String endCustomerCountryName;
        private String goToMarketCode;
        private String deactivated;
        private String tenantPrimaryEmail;
        private String endCustomerCRSID;
        private String appsite;
        private String endCustomerOPSIID;
        private String tenantName;
        private String resellerOPSIID;
        private String endCustomerCountryCode;
        private String objectOfServiceDescription;
        private String endCustomerRegionName;
        private String endCustomerPostalCode;
        private String planId;
        private String resellerCRSID;
        private String id;
        private String modified_at;
        private String bom3;
        private String serviceEndDate;
        private String endCustomerAddressLine1;
        private String objectOfServiceProductId;
        private String resellerOPSID;
        private String resellerName;
        private String deactivatedReason;
        private String endCustomerOPSID;
        private String rootTenantId;
        private String resellerId;
        private String last_attempted_date_gcw;
        private String packDescription;
        private String serviceStartDate;
        private String gcwStatusCode;
        private String hpOrderId;
        private String deviceSrNo;
        private String deviceProductNo;
        private String endCustomerName;
        private String fcpkProductId;
        private String planDisplayId;
        private String endCustomerId;
        private String fcpkSerialNumber;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Device {
        private String serviceStartDate;
        private String lastDtAttempted;
        private String packDescription;
        private String deviceId;
        private String goToMarketCode;
        private String serviceEndDate;
        private String resellerId;
        @JsonProperty("last_attempted_date_gcw")
        private String lastAttemptedDateGcw;
        @JsonProperty("appsite")
        private String appSite;
        private String assetId;
        private String gcwStatusCode;
        private String endCustomerRegionName;
        private String tenantId;
        private String deviceSrNo;
        private String planId;
        private String tenantEndCustomerName;
        private String planDisplayId;
        private String primaryEmail;
        private String status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class CamsAndAssetData {
        @JsonProperty("cams_data")
        private List<CamsData> camsData;
        private List<AssetData> assets;
        private List<Reseller> reseller;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class CamsData {
        private String endCustomerPrimaryEmail;
        private String endCustomerStateProvince;
        private String endCustomerCountryName;
        private String goToMarketCode;
        private String deactivated;
        private String endCustomerCRSID;
        private String licenseRemovalFlow;
        private String endCustomerOPSIID;
        private String appsite;
        private String resellerOPSIID;
        private String endCustomerCountryCode;
        private String objectOfServiceDescription;
        private String endCustomerRegionName;
        private String endCustomerPostalCode;
        private String resellerCRSID;
        private String planId;
        private String bom3;
        private String serviceEndDate;
        private String endCustomerAddressLine1;
        private String objectOfServiceProductId;
        private String resellerOPSID;
        private String resellerName;
        private String deactivatedReason;
        private String endCustomerOPSID;
        private String last_attempted_date_gcw;
        private String packDescription;
        private String serviceStartDate;
        private String licenseStatusUpdatedAt;
        private String gcwStatusCode;
        private String licenseStatus;
        private String hpOrderId;
        private String deviceSrNo;
        private String deviceProductNo;
        private String endCustomerName;
        private String fcpkProductId;
        private String fcpkSerialNumber;
        private String endCustomerId;
        private String planDisplayId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class AssetData {
        private String appsite;
        private String tenantName;
        private String assetId;
        private String tenantId;
        private String wxp_state;
        private String displayId;
        private String deviceId;
        private String primaryEmail;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Reseller {
        private String partnerResellerId;
        private String lastDtAttempted;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class AssetMongoProvider {
        private String tenantId;

        @JsonProperty("plans")
        private Plans plans;

        @JsonProperty("status")
        private Status status;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Plans {
            @JsonProperty("en")
            private NameValue en;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Status {
            @JsonProperty("en")
            private NameValue en;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class NameValue {
            private String name;
            private String value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class DynamoDbProvider {
        @JsonProperty("DeviceHashDetails")
        private DeviceHashDetails deviceHashDetails;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class DeviceHashDetails {
            @JsonProperty("DeviceId")
            private String deviceId;
            @JsonProperty("TenantId")
            private String tenantId;
            @JsonProperty("ServiceName")
            private String serviceName;
        }
    }
}