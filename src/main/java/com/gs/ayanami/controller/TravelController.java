package com.gs.ayanami.controller;

import com.gs.ayanami.dto.PlanRequest;
import com.gs.ayanami.service.DifyService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class TravelController {
    private final DifyService difyService;

    public TravelController(DifyService difyService) {
        this.difyService = difyService;
    }

    @PostMapping(value = "/plan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> createPlan(@Valid @RequestBody PlanRequest req) {
        return difyService.invoke(req);
    }
}
