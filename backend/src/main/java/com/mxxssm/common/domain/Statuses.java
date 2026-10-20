package com.mxxssm.common.domain;

public final class Statuses {

    public static final String ACTIVE = "ACTIVE";
    public static final String DISABLED = "DISABLED";

    private Statuses() {
    }

    public static boolean isActive(String status) {
        return ACTIVE.equalsIgnoreCase(status);
    }

    public static boolean isValid(String status) {
        return ACTIVE.equals(status) || DISABLED.equals(status);
    }
}
