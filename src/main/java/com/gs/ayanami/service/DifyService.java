package com.gs.ayanami.service;

import com.gs.ayanami.dto.PlanRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;



@Service
public class DifyService {
    @Value("${DIFY_BASE_URL:https://api.dify.ai}")
    private String baseUrl;

    @Value("${DIFY_API_KEY:your-default-api-key}")
    private String apiKey;

    @Value("${DIFY_APP_ID:your-default-app-id}")
    private String appId;

    private final WebClient client;
    private final RestTemplate restTemplate = new RestTemplate();
    public DifyService(
            @Value("${DIFY_BASE_URL:https://api.dify.ai}") String baseUrl,
            @Value("${DIFY_API_KEY:}") String apiKey,
            @Value("${DIFY_APP_ID:}") String appId
    ) {
        this.baseUrl = baseUrl;
        this.appId = appId;
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 将请求转给 Dify 应用并返回模型生成的文本（简化：获取 response.text 或 choices[0].content）。
     * 你可能需要根据你 Dify 的返回做调整。
     */
    public Mono<String> invoke(PlanRequest req) {
        // --- 这里的请求体结构是一个通用示例，按你 Dify 应用的实际要求修改 ---
        var payload = java.util.Map.of(
                "input", java.util.Map.of(
                        "destination", req.getDestination(),
                        "day", req.getDay(),
                        "budget", req.getBudget()
                ),
                "user", "test-user-001"
        );

        // Example endpoint: POST {baseUrl}/v1/apps/{appId}/invoke
        String path = "/v1/apps/" + appId + "/invoke";

        return client.post()
                .uri(path)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class) // 直接返回原始 JSON 字符串，前端可解析
                .onErrorResume(e -> Mono.just("{\"error\":\"" + e.getMessage() + "\"}"));
    }
}
