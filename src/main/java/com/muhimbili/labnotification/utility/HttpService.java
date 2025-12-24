package com.muhimbili.labnotification.utility;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Service
public class HttpService {
    private static final String ERROR_LOG_LEVEL = "error";

    private final RestTemplate restTemplate;
    private final OkHttpClient okHttpClient;
    public final Gson jsonProcessor;
    private final LoggerService logger;

    public HttpService(RestTemplate restTemplate, Gson jsonProcessor, LoggerService logger) {
        this.restTemplate = restTemplate;
        this.jsonProcessor = jsonProcessor;
        this.logger = logger;
        this.okHttpClient = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(1200))
                .connectTimeout(Duration.ofSeconds(1200))
                .readTimeout(Duration.ofSeconds(1200))
                .build();
    }

    public <T> T postJson(String url, String jsonString, Class<T> clazz) {
        return executePost(url, jsonString, clazz, Collections.singletonMap("Content-Type", "application/json"));
    }

    public <T> T postJson(String url, String jsonString, Map<String, String> headers, Class<T> clazz) {
        Map<String, String> headerMap = headers == null ? Collections.emptyMap() : headers;
        if (!headerMap.containsKey("Content-Type")) {
            headerMap = new java.util.HashMap<>(headerMap);
            headerMap.put("Content-Type", "application/json");
        }
        return executePost(url, jsonString, clazz, headerMap);
    }

    public <T> T postXml(String url, String xml, Map<String, String> customHeader, Class<T> clazz) {
        Map<String, String> headers = customHeader == null ? Collections.emptyMap() : customHeader;
        return executePost(url, xml, clazz, headers);
    }

    private <T> T executePost(String url, String payload, Class<T> clazz, Map<String, String> headersMap) {
        HttpHeaders headers = new HttpHeaders();
        if (!headersMap.containsKey("Content-Type")) {
            headers.add("Content-Type", "application/json");
        }
        headersMap.forEach(headers::add);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, clazz);
            return responseEntity.getBody();
        } catch (Exception e) {
            logError("POST", url, e);
            return null;
        }
    }

    public <T> T getUrl(String url, Class<T> clazz) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            return response.getBody() == null ? null : jsonProcessor.fromJson(response.getBody(), clazz);
        } catch (Exception e) {
            logError("GET", url, e);
            return null;
        }
    }

    public <T> T getDataUrl(String url, Class<T> clazz) {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() == null) {
                logger.log("GET " + url + " returned empty body", ERROR_LOG_LEVEL);
                return null;
            }
            String responseBody = response.body().string();
            return jsonProcessor.fromJson(responseBody, clazz);
        } catch (IOException e) {
            logError("GET", url, e);
            return null;
        }
    }

    public String getRaw(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() == null) {
                logger.log("GET " + url + " returned empty body", ERROR_LOG_LEVEL);
                return null;
            }
            return response.body().string();
        } catch (IOException e) {
            logError("GET", url, e);
            return null;
        }
    }

    public String getTokenProcessor(RequestBody requestBody, Headers headers, String url) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .headers(headers)
                .build();
        try (Response httpCallResponse = okHttpClient.newCall(request).execute()) {
            if (httpCallResponse.body() == null) {
                logger.log("POST " + url + " returned empty body", ERROR_LOG_LEVEL);
                return null;
            }
            String response = httpCallResponse.body().string();
            logger.log(response);
            return response;
        } catch (IOException e) {
            logError("POST", url, e);
            return "EXCEPTION - " + e.getMessage();
        }
    }

    private void logError(String method, String url, Exception e) {
        String message = String.format("%s request to %s failed: %s", method, url, e.getMessage());
        logger.log(message, ERROR_LOG_LEVEL);
    }
}
