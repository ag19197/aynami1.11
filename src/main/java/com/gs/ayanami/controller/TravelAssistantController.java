package com.gs.ayanami.controller;

import com.gs.ayanami.service.TravelAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/travel")
public class TravelAssistantController {

    @Autowired
    private TravelAssistantService travelAssistantService;

    @PostMapping("/plan")
    public ResponseEntity<String> plan(@RequestBody Map<String, String> params) {
        String destination = params.get("destination");
        String day = params.get("day");
        String budget = params.get("budget");

        String result = travelAssistantService.getTravelPlan(destination, day, budget);
        return ResponseEntity.ok(result);
    }
}