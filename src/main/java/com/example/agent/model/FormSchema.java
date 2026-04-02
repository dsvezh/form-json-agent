package com.example.agent.model;

import java.util.ArrayList;
import java.util.List;

public class FormSchema {
    private String url;
    private List<FieldDescriptor> fields = new ArrayList<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<FieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(List<FieldDescriptor> fields) {
        this.fields = fields;
    }
}
