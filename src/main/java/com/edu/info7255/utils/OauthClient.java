package com.edu.info7255.utils;

import com.edu.info7255.BaseClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class OauthClient extends BaseClient {


    public boolean verify(String token) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            ResponseEntity response = getCall(url);
            if (response.getStatusCode() == HttpStatusCode.valueOf(200)) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error while verifying token : " + e);
        }
        return false;
    }
}
