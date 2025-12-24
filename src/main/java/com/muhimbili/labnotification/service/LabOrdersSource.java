package com.muhimbili.labnotification.service;

/**
 * Source for fetching lab orders.
 */
public enum LabOrdersSource {
    REMOTE,
    MOCK;

    public static LabOrdersSource from(String value) {
        if (value == null) {
            return REMOTE;
        }
        for (LabOrdersSource source : values()) {
            if (source.name().equalsIgnoreCase(value.trim())) {
                return source;
            }
        }
        return REMOTE;
    }
}
