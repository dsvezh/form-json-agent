package com.example.agent.model;

import java.util.ArrayList;
import java.util.List;

public class FieldDescriptor {
    private String key;
    private String label;
    private String type;
    private boolean required;
    private List<FieldOption> options = new ArrayList<>();

    public FieldDescriptor() {
    }

    public FieldDescriptor(String key, String label, String type, boolean required) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<FieldOption> getOptions() {
        return options;
    }

    public void setOptions(List<FieldOption> options) {
        this.options = options;
    }
}
