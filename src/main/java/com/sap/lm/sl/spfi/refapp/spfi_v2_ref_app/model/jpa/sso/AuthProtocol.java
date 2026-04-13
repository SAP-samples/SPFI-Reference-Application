package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AuthProtocol {
    SAML("saml2"), OpenIDConnect("oidc"), Kerberos("kerberos");

    private final String authProtocol;

    AuthProtocol(String protocol) {
        this.authProtocol = protocol;
    }

    // This method maps the JSON string to the corresponding enum value
    @JsonCreator
    public static AuthProtocol fromString(String protocol) {
        if (protocol == null) {
            return null; // or throw IllegalArgumentException if needed
        }
        return switch (protocol.toLowerCase()) {
            case "saml", "saml2" -> SAML;
            case "oidc" -> OpenIDConnect;
            case "kerberos" -> Kerberos;
            default -> throw new IllegalArgumentException("Unknown protocol: " + protocol);
        };

    }

    // This is optional, for serializing enum back to JSON if needed
    @JsonValue
    public String getAuthProtocol() {
        return authProtocol;
    }
}
