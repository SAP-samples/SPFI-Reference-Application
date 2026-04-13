package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Setter
@Getter
public class Customer {
    @NotBlank(message = "customer id cannot be null or blank")
    private String id;
    private String erpId;
    private String name;

    @Valid
    private Contact contact;

}
