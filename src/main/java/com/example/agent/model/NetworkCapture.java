package com.example.agent.model;

/**
 * Хранит краткую информацию о перехваченном сетевом запросе.
 * Обычно нас интересуют JSON-запросы, которые отправляет форма.
 */
public class NetworkCapture {
    private String method;
    private String url;
    private String postData;
    private String contentType;

    public NetworkCapture() {
    }

    public NetworkCapture(String method, String url, String postData, String contentType) {
        this.method = method;
        this.url = url;
        this.postData = postData;
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
