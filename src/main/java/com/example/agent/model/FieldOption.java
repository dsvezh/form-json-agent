package com.example.agent.model;

/**
 * Одна опция выпадающего списка.
 * value - то, что обычно уходит в API или хранится во внутреннем состоянии.
 * label - то, что видит пользователь на экране.
 */
public record FieldOption(String value, String label) {
}
