package enums;

import lombok.Getter;

@Getter
public enum OrderType {
    ACTIVE_CARE("ActiveCare"),
    PREMIUM("Premium"),
    PREMIUM_PLUS("PremiumPlus"),
    OUT_OF_BAND_REMEDIATION("OutOfBandRemediation"),
    DEVICE_TRANSFER("DeviceTransfer"),
    PARTNER_DEVICE_TRANSFER_AC_TO_ACPI("PartnerDeviceTransferAcToAcpi"),
    PARTNER_DEVICE_TRANSFER_ACPI_TO_AC("PartnerDeviceTransferAcpiToAc"),
    NONE("None");

    private final String baseName;

    OrderType(String baseName) {
        this.baseName = baseName;
    }
}
