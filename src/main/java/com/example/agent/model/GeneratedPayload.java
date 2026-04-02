package com.example.agent.model;

import java.util.Map;

/**
 * Один сгенерированный сценарий:
 * flatValues - плоские значения полей формы,
 * payload - итоговый nested JSON,
 * sourceRequestUrl/sourceRequestBody - если удалось перехватить реальный запрос страницы.
 */
public class GeneratedPayload {
    private Map<String, Object> flatValues;
    private Map<String, Object> payload;
    private String sourceRequestUrl;
    private String sourceRequestBody;

    public GeneratedPayload() {
    }

    public GeneratedPayload(Map<String, Object> flatValues,
                            Map<String, Object> payload,
                            String sourceRequestUrl,
                            String sourceRequestBody) {
        this.flatValues = flatValues;
        this.payload = payload;
        this.sourceRequestUrl = sourceRequestUrl;
        this.sourceRequestBody = sourceRequestBody;
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

    public String getSourceRequestUrl() {
        return sourceRequestUrl;
    }

    public void setSourceRequestUrl(String sourceRequestUrl) {
        this.sourceRequestUrl = sourceRequestUrl;
    }

    public String getSourceRequestBody() {
        return sourceRequestBody;
    }

    public void setSourceRequestBody(String sourceRequestBody) {
        this.sourceRequestBody = sourceRequestBody;
    }
}
