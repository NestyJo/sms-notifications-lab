package com.muhimbili.labnotification.service.sms.client;

import java.util.List;

public record KilakonaMessageRequest(
        String senderId,
        String messageType,
        String message,
        String contacts,
        String deliveryReportUrl
) {
    public static KilakonaMessageRequest text(String senderId,
                                              String message,
                                              List<String> contacts,
                                              String deliveryReportUrl) {
        String contactsJoined = String.join(",", contacts);
        return new KilakonaMessageRequest(senderId, "text", message, contactsJoined, deliveryReportUrl);
    }
}
