package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class Tenant {
    private String id;
    private String globalTenantId;

    @NotBlank(message = "sapId cannot be null or blank")
    private String sapId;

    @Valid
    @NotNull(message = "customer cannot be null")
    private Customer customer;
    private String businessType;
    private String operationType;

    @Valid
    @NotNull(message = "location cannot be null")
    private Location location;
    private Endpoints endpoints;

    @Valid
    @Size(min = 1, message = "At least one initial user is required.")
    private List<InitialUser> initialUsers;

    @Valid
    @Size(min = 1, message = "At least one SSO provider is required.")
    private List<Sso> sso;

    private ContractValidity contract;
    private List<Product> products;

    private Status status;
    private AdditionalProperties additionalProperties;

    private Resource hostTenantSpecification;
    private List<Resource> mixinSpecifications;

}

