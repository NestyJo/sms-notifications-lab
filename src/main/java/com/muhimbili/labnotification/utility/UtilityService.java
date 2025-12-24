package com.muhimbili.labnotification.utility;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

@Service
public class
UtilityService {


    private final DateUtility dateUtility;
    private final Gson jsonProcessor;
    private final HttpService httpService;
    private final StringUtility stringUtility;

    private static final String JSON_FILE_PATH = "data.json";

    public UtilityService(DateUtility dateUtility, Gson jsonProcessor, HttpService httpService, StringUtility stringUtility) {
        this.dateUtility = dateUtility;
        this.jsonProcessor = jsonProcessor;
        this.httpService = httpService;
        this.stringUtility = stringUtility;
    }

    public DateUtility getDateUtility() {
        return dateUtility;
    }

    public Gson getJsonProcessor() {
        return jsonProcessor;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public StringUtility getStringUtility() {
        return stringUtility;
    }

    public JsonMapper jsonMapper() {
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return jsonMapper;
    }

    public String generateUniqueId() {
        return dateUtility.generateTimePrefix() + stringUtility.randomString();
    }

}
