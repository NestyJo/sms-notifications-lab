package com.muhimbili.labnotification.utility;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BeanManager {
    @Bean
    public Gson gson(){ return new Gson(); }

    @Bean
    public OkHttpClient httpClient() { return new OkHttpClient(); }

    @Bean
    public RestTemplate restTemplateBean(){
        return new RestTemplate();
    }
}
