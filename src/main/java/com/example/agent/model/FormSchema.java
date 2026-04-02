package com.example.agent.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Нормализованное описание всей формы.
 * Это главный артефакт, который строится после обхода страницы.
 */
public class FormSchema {
    private String url;
    private List<FieldDescriptor> fields = new ArrayList<>();
    private List<FieldDependencySnapshot> dependencySnapshots = new ArrayList<>();

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

    public List<FieldDependencySnapshot> getDependencySnapshots() {
        return dependencySnapshots;
    }

    public void setDependencySnapshots(List<FieldDependencySnapshot> dependencySnapshots) {
        this.dependencySnapshots = dependencySnapshots;
    }
}
