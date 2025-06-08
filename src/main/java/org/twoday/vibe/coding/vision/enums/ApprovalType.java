package org.twoday.vibe.coding.vision.enums;

public enum ApprovalType {
    BASIC("basic"),
    COMITET("comitet"),
    BASIC_DIRECTOR("basic+director"),
    COMITET_DIRECTOR("comitet+director");

    private final String value;

    ApprovalType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ApprovalType fromValue(String value) {
        for (ApprovalType type : ApprovalType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown approval type: " + value);
    }

    @Override
    public String toString() {
        return this.value;
    }
} 