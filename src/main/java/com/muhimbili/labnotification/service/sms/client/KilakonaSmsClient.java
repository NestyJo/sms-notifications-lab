package com.muhimbili.labnotification.service.sms.client;

import com.google.gson.Gson;
import com.muhimbili.labnotification.utility.ApplicationProp;
import com.muhimbili.labnotification.utility.HttpService;
import com.muhimbili.labnotification.utility.LoggerService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KilakonaSmsClient {

    private final ApplicationProp applicationProp;
    private final HttpService httpService;
    private final LoggerService loggerService;
    private final Gson gson;

    public KilakonaSmsClient(ApplicationProp applicationProp,
                             HttpService httpService,
                             LoggerService loggerService,
                             Gson gson) {
        this.applicationProp = applicationProp;
        this.httpService = httpService;
        this.loggerService = loggerService;
        this.gson = gson;
    }

    public KilakonaMessageResponse sendText(String message, List<String> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            loggerService.warn("kilakona_sms -> no contacts provided, skipping send");
            return new KilakonaMessageResponse(400, null, false, "No contacts provided");
        }

        KilakonaMessageRequest payload = KilakonaMessageRequest.text(
                applicationProp.getSmsGatewaySenderId(),
                message,
                contacts,
                applicationProp.getSmsDeliveryReportUrl()
        );

        Map<String, String> headers = new HashMap<>();
        headers.put("api_key", applicationProp.getSmsGatewayApiKey());
        headers.put("api_secret", applicationProp.getSmsGatewayApiSecret());

        String url = applicationProp.getSmsGatewaySendUrl();
        KilakonaMessageResponse response = httpService.postJson(
                url,
                gson.toJson(payload),
                headers,
                KilakonaMessageResponse.class
        );

        if (response == null) {
            loggerService.error("kilakona_sms -> gateway returned null response");
            return new KilakonaMessageResponse(500, null, false, "Gateway error");
        }

        loggerService.debug("kilakona_sms -> response code={} success={} message={} shootId={}",
                response.code(), response.success(), response.message(),
                response.data() != null ? response.data().shootId() : "<none>");
        return response;
    }
}
