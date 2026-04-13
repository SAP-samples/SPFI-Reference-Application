package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AdditionalProperties {
    private Map<String, Object> fromManager;
    private Map<String, Object> fromProvider;

/*    @JsonAnySetter
    public void addAdditionalPropertyFromManager(String key, Object value) {
        fromManager.put(key, value);
    }

    @JsonAnySetter
    public void addAdditionalPropertyFromProvider(String key, Object value) {
        fromProvider.put(key, value);
    }*/
}
