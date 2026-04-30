/*
 * Copyright (c) 2026 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings;

public class Setting {
    private String name;
    private SettingType type;
    private Object value;

    /**
     * Retrieves {@link #name}
     *
     * @return value of {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets {@link #name} value
     *
     * @param name new value of {@link #name}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves {@link #type}
     *
     * @return value of {@link #type}
     */
    public SettingType getType() {
        return type;
    }

    /**
     * Sets {@link #type} value
     *
     * @param type new value of {@link #type}
     */
    public void setType(SettingType type) {
        this.type = type;
    }

    /**
     * Retrieves {@link #value}
     *
     * @return value of {@link #value}
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets {@link #value} value
     *
     * @param value new value of {@link #value}
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
