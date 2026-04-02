package com.example.agent.model;

import java.util.Map;

public class GeneratedPayload {
    private Map<String, Object> flatValues;
    private Map<String, Object> payload;

    public GeneratedPayload() {
    }

    public GeneratedPayload(Map<String, Object> flatValues, Map<String, Object> payload) {
        this.flatValues = flatValues;
        this.payload = payload;
    }

    public Map<String, Object> getFlatValues() {
        return flatValues;
    }

    public void setFlatValues(Map<String, Object> flatValues) {
        this.flatValues = flatValues;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
