package com.gs.ayanami.service;

import com.gs.ayanami.config.TravelAssistantConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TravelAssistantService {

    private final TravelAssistantConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    public TravelAssistantService(TravelAssistantConfig config) {
        this.config = config;
    }

    public String getTravelPlan(String destination, String day, String budget) {
        // 你要请求的 Dify endpoint（根据你实际使用的 endpoint 调整）
        String url = config.getBaseUrl() + "/completion-messages"; // 或 /apps/{app_id}/workflow/run

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // setBearerAuth 可能在老版本 Spring 不可用，下面先 try 再 fallback：
        try {
            headers.setBearerAuth(config.getApiKey());
        } catch (Throwable t) {
            headers.set("Authorization", "Bearer " + config.getApiKey());
        }

        Map<String, Object> requestBody = Map.of(
                "inputs", Map.of(
                        "destination", destination,
                        "day", day,
                        "budget", budget
                ),
                "response_mode", "blocking"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        return resp.getBody();
    }
}