package com.sap.lm.sl.spfi.operations.client.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class AdditionalProperties {
    private Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
