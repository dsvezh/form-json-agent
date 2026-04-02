package com.example.agent.browser;

import com.example.agent.model.NetworkCapture;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Слушает сетевые запросы страницы.
 * Это полезно, когда нужно не угадывать JSON-структуру, а посмотреть,
 * что фронтенд реально отправляет на сервер.
 */
public class NetworkInterceptor {
    private final List<NetworkCapture> captures = new ArrayList<>();

    public void attach(Page page, String urlContains) {
        page.onRequest(this::handleRequest);
    }

    private void handleRequest(Request request) {
        String postData = request.postData();
        String contentType = request.headerValue("content-type");

        // Нас в первую очередь интересуют JSON-запросы.
        if (postData == null || postData.isBlank()) {
            return;
        }

        captures.add(new NetworkCapture(
            request.method(),
            request.url(),
            postData,
            contentType == null ? "" : contentType
        ));
    }

    /**
     * Возвращает только те запросы, которые похожи на JSON и при необходимости
     * соответствуют фильтру по URL.
     */
    public List<NetworkCapture> getRelevantCaptures(String urlContains) {
        return captures.stream()
                       .filter(capture -> capture.getContentType().contains("json")
                                          || looksLikeJson(capture.getPostData()))
                       .filter(capture -> urlContains == null
                                          || urlContains.isBlank()
                                          || capture.getUrl().contains(urlContains))
                       .toList();
    }

    private boolean looksLikeJson(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }
}
