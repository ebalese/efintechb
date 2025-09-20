package com.efintechb.lbsite.statistics.model;

public enum DeviceType {
    IOS,
    ANDROID,
    WATCH,
    TV;

    public static boolean isValid(String value) {
        if (value == null) return false;
        try {
            DeviceType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
