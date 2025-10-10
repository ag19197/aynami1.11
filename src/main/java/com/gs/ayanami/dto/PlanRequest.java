package com.gs.ayanami.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlanRequest {
    @NotBlank
    private String destination;
    @Min(1)
    private int day;
    @NotBlank
    private String budget;

    private String userId;

    public PlanRequest(String destination, int day, String budget, String userId) {
        this.destination = destination;
        this.day = day;
        this.budget = budget;
        this.userId = userId;
    }

    public PlanRequest() {
    }

    // getters / setters
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
