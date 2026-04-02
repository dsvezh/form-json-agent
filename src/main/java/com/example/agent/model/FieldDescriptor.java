package com.example.agent.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Полное описание одного поля формы.
 * Такой объект удобен тем, что дальше генератору и билдеру JSON уже не нужно
 * читать DOM-элементы напрямую: достаточно работать с этой моделью.
 */
public class FieldDescriptor {
    private String key;
    private String label;
    private String type;
    private boolean required;
    private boolean customDropdown;
    private String selector;
    private String dependsOn;
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

    public boolean isCustomDropdown() {
        return customDropdown;
    }

    public void setCustomDropdown(boolean customDropdown) {
        this.customDropdown = customDropdown;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }

    public List<FieldOption> getOptions() {
        return options;
    }

    public void setOptions(List<FieldOption> options) {
        this.options = options;
    }
}
