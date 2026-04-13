package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
public class StateRequest {
    @NotBlank(message = "state cannot be null or blank")
    @Pattern(regexp = "^(active|blocked)$", message = "state must be one of active or blocked")
    private String state;
}