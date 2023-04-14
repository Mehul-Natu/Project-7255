package com.edu.info7255;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BaseClient {

    protected ResponseEntity getCall(String url) {
        ResponseEntity<String> response = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForEntity(url, String.class);
        } catch (Exception e) {
            System.out.println("Exception at Base Get call: " + e);
            return null;
        }
    }

}
