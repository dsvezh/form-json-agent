package com.example.agent.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Снимок зависимого поля в конкретном состоянии.
 * Пример: country=US -> state имеет опции [CA, NY].
 */
public class FieldDependencySnapshot {
    private String parentField;
    private String parentValue;
    private String childField;
    private List<FieldOption> options = new ArrayList<>();

    public String getParentField() {
        return parentField;
    }

    public void setParentField(String parentField) {
        this.parentField = parentField;
    }

    public String getParentValue() {
        return parentValue;
    }

    public void setParentValue(String parentValue) {
        this.parentValue = parentValue;
    }

    public String getChildField() {
        return childField;
    }

    public void setChildField(String childField) {
        this.childField = childField;
    }

    public List<FieldOption> getOptions() {
        return options;
    }

    public void setOptions(List<FieldOption> options) {
        this.options = options;
    }
}
