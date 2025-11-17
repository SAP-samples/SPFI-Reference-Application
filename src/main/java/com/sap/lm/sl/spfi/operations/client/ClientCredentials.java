package com.sap.lm.sl.spfi.operations.client;

public class ClientCredentials {
    private String iasUrl;
    private String clientId;
    private String clientSecret;

    public ClientCredentials(String iasUrl, String clientId, String clientSecret) {
        this.iasUrl = iasUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getIasUrl() {
        return iasUrl;
    }

    public void setIasUrl(String iasUrl) {
        this.iasUrl = iasUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
