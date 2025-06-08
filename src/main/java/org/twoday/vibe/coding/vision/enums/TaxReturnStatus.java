package org.twoday.vibe.coding.vision.enums;

public enum TaxReturnStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String value;

    TaxReturnStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaxReturnStatus fromValue(String value) {
        for (TaxReturnStatus status : TaxReturnStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown tax return status: " + value);
    }

    @Override
    public String toString() {
        return this.value;
    }
} 