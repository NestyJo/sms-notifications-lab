package com.muhimbili.labnotification.service.sms.client;

public record KilakonaMessageResponse(
        int code,
        Payload data,
        boolean success,
        String message
) {

    public boolean isSuccessful() {
        return success;
    }

    public record Payload(
            int validContacts,
            int invalidContacts,
            int duplicatedContacts,
            int messageSize,
            String message,
            String shootId
    ) {
    }
}
