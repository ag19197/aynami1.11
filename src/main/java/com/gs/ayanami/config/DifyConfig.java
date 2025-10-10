package com.gs.ayanami.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dify")
public class DifyConfig {
    private String baseUrl;
    private String apiKey;
    private String appId;

    // getter & setter
}
