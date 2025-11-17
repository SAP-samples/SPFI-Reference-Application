package com.sap.lm.sl.spfi.operations.client;

import org.apache.http.client.HttpClient;

public class SpfiOperationsClientFactory {
    private static final SpfiOperationsClientFactory INSTANCE = new SpfiOperationsClientFactory();

    private SpfiOperationsClientFactory() {
    }

    public static SpfiOperationsClientFactory getInstance() {
        return INSTANCE;
    }

    public ISpfiOperationsClient createSpfiOperationsClient(HttpClient httpClient, String baseUrl, ClientCredentials clientCredentials) {
        return new SpfiOperationsClient(httpClient, baseUrl, clientCredentials);
    }

}
