package com.muhimbili.labnotification.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.muhimbili.labnotification.data.response.ExternalLabResultsResponse;
import com.muhimbili.labnotification.data.response.LabResultsPreviewResponse;
import com.muhimbili.labnotification.utility.ApplicationProp;
import com.muhimbili.labnotification.utility.HttpService;
import com.muhimbili.labnotification.utility.LoggerService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class LabOrdersService {

    private final HttpService httpService;
    private final ApplicationProp applicationProp;
    private final LoggerService loggerService;
    private final LabResultsTransformer labResultsTransformer;
    private final LabResultsPersistenceService labResultsPersistenceService;
    private final Gson gson;

    public LabOrdersService(HttpService httpService,
                            ApplicationProp applicationProp,
                            LoggerService loggerService,
                            LabResultsTransformer labResultsTransformer,
                            LabResultsPersistenceService labResultsPersistenceService,
                            Gson gson) {
        this.httpService = httpService;
        this.applicationProp = applicationProp;
        this.loggerService = loggerService;
        this.labResultsTransformer = labResultsTransformer;
        this.labResultsPersistenceService = labResultsPersistenceService;
        this.gson = gson;
    }

    public LabResultsPreviewResponse fetchOrders(String date, String fromTime, String toTime) {
        String rawResponse = fetchRawResponse(date, fromTime, toTime);
        loggerService.info("lab_orders_service -> raw response={}", formatJson(rawResponse));

        ExternalLabResultsResponse externalResponse = parseResponse(rawResponse);
        if (externalResponse != null) {
            labResultsPersistenceService.persist(externalResponse);
        }

        LabResultsPreviewResponse previewResponse = labResultsTransformer.transform(externalResponse);
        loggerService.info("lab_orders_service -> preview response generated with {} patients",
                previewResponse.getTotalPatients());
        return previewResponse;
    }

    private String fetchRawResponse(String date, String fromTime, String toTime) {
        LabOrdersSource source = LabOrdersSource.from(applicationProp.getLabOrdersSource());
        if (source == LabOrdersSource.MOCK) {
            String resourceName = applicationProp.getLabOrdersMockResource();
            loggerService.info("lab_orders_service -> using MOCK response from classpath resource={}", resourceName);
            return readClasspathResource(resourceName);
        }

        String url = String.format("%s/%s/%s/%s",
                applicationProp.getLabOrdersBaseUrl(),
                encode(date),
                encode(fromTime),
                encode(toTime));

        loggerService.info("lab_orders_service -> calling endpoint={}", url);
        return httpService.getRaw(url);
    }

    private String readClasspathResource(String resourceName) {
        if (resourceName == null || resourceName.isBlank()) {
            loggerService.warn("lab_orders_service -> mock resource not configured");
            return null;
        }

        Resource resource = new ClassPathResource(resourceName);
        if (!resource.exists()) {
            loggerService.error("lab_orders_service -> mock resource not found on classpath: {}", resourceName);
            return null;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            loggerService.error("lab_orders_service -> failed reading mock resource {}: {}", resourceName, e.getMessage());
            return null;
        }
    }

    private ExternalLabResultsResponse parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(rawResponse, ExternalLabResultsResponse.class);
        } catch (Exception e) {
            loggerService.error("lab_orders_service -> failed to parse response: {}", e.getMessage());
            return null;
        }
    }

    private String formatJson(String body) {
        if (body == null || body.isEmpty()) {
            return "<empty>";
        }
        try {
            JsonElement jsonElement = JsonParser.parseString(body);
            return jsonElement.toString();
        } catch (Exception e) {
            return body;
        }
    }

    private String encode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
