package com.muhimbili.labnotification.utility;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@Getter
public class ApplicationProp {

    @Value("${lab.orders.base-url}")
    private String labOrdersBaseUrl;

    /**
     * Controls where lab orders are fetched from.
     * Supported values: "remote" (call external endpoint) or "mock" (load JSON from classpath).
     */
    @Value("${lab.orders.source:remote}")
    private String labOrdersSource;

    /**
     * Classpath resource containing a sample external response payload.
     * Example: Response.json
     */
    @Value("${lab.orders.mock-resource:Response.json}")
    private String labOrdersMockResource;

    @Value("${sms.message-template:Dear %s, your lab results are ready. Order: %s}")
    private String smsMessageTemplate;

    @Value("${sms.message-template-swahili:Ndugu %s, majibu yako ya maabara yako yametumwa kwa daktari. Asante.}")
    private String smsMessageTemplateSwahili;

    @Value("${sms.batch-size:100}")
    private int smsBatchSize;

    @Value("${sms.provider-id:MLOGANZILA}")
    private String smsProviderId;

    @Value("${sms.gateway.base-url:https://messaging.kilakona.co.tz/api/v1}")
    private String smsGatewayBaseUrl;

    @Value("${sms.gateway.send-path:/vendor/message/send}")
    private String smsGatewaySendPath;

    @Value("${sms.gateway.api-key:chimwege}")
    private String smsGatewayApiKey;

    @Value("${sms.gateway.api-secret:f5IZEc5o8PeXi9l5ilOo}")
    private String smsGatewayApiSecret;

    @Value("${sms.gateway.sender-id:MLG}")
    private String smsGatewaySenderId;

    @Value("${sms.gateway.sender-name:MLG}")
    private String smsGatewaySenderName;

    @Value("${sms.gateway.delivery-report-url:}")
    private String smsDeliveryReportUrl;

    @Value("${sms.gateway.timeout-seconds:30}")
    private int smsGatewayTimeoutSeconds;

    @Value("${sms.gateway.verify-ssl:false}")
    private boolean smsGatewayVerifySsl;

    public boolean isLabOrdersMockEnabled() {
        return labOrdersSource != null && labOrdersSource.equalsIgnoreCase("mock");
    }

    public String getSmsGatewaySendUrl() {
        String base = smsGatewayBaseUrl != null ? smsGatewayBaseUrl : "";
        base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String path = smsGatewaySendPath == null ? "" : smsGatewaySendPath;
        path = path.startsWith("/") ? path : "/" + path;
        return base + path;
    }
}
