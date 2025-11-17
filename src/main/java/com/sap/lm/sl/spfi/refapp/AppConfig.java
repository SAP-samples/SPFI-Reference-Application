package com.sap.lm.sl.spfi.refapp;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.sap.lm.sl.spfi.refapp.controllers.AppException;

@Configuration
public class AppConfig {
    public static final String ENV_TENANT_OPERATOR_CREDENTIALS = "TENANT_OPERATOR_CREDENTIALS";
    public static final String ENV_IDENTITY_CREDENTIALS = "IDENTITY_CREDENTIALS";
    public static final String ENV_APPLICATION_ENDPOINT = "APPLICATION_ENDPOINT";
    public static final String ENV_NOTIFICATION_ID_KEY = "NOTIFICATION_ID_KEY";


    private String iasUrl;
    private String clientId;
    private String clientSecret;
    private String tenantOpClientId;
    private String tenantOpIssuer;
    private String appEndpoint;
    private String key;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AppConfig() {
        try {
            readConfigFromEnv();
        } catch (AppException e) {
            logger.error("Failed to read configuration");
        }
    }

    private void readConfigFromEnv() throws AppException {
        JSONObject credentialsJson;

        try {
            credentialsJson = new JSONObject(getIdentityCredentialsFromEnv());
            iasUrl = credentialsJson.getString("url");
            clientId = credentialsJson.getString("clientid");
            clientSecret = credentialsJson.getString("clientsecret");
        } catch (JSONException e) {
            logger.error("Could not parse credentials", e);
            throw new AppException("Could not parse credentials", e);
        }

        try {
            credentialsJson = new JSONObject(getTenantOpCredentialsFromEnv());
            tenantOpClientId = credentialsJson.getString("clientid");
            tenantOpIssuer = credentialsJson.getString("issuerName");
        } catch (JSONException e) {
            logger.error("Could not parse credentials", e);
            throw new AppException("Could not parse credentials", e);
        }

        appEndpoint = getAppEndpointFromEnv();
        key = getEnv(ENV_NOTIFICATION_ID_KEY);

//        logger.debug("iasUrl: " + iasUrl);
//        logger.debug("clientId: " + clientId);
//        logger.debug("clientSecret: " + clientSecret);
//        logger.debug("tenantOpClientId: " + tenantOpClientId);
//        logger.debug("tenantOpIssuer: " + tenantOpIssuer);
    }

    private String getIdentityCredentialsFromEnv() throws AppException {
        String spfiIdentityCredentialsEnv = getEnv(ENV_IDENTITY_CREDENTIALS);
        if (spfiIdentityCredentialsEnv == null) {
            logger.error("Credentials to identity service are not provided");
            throw new AppException("Credentials to identity service are not provided");
        }
        return spfiIdentityCredentialsEnv;
    }

    private String getTenantOpCredentialsFromEnv() throws AppException {
        String tenantOpCredentialsEnv = getEnv(ENV_TENANT_OPERATOR_CREDENTIALS);
        if (tenantOpCredentialsEnv == null) {
            logger.error("Credentials to tenant operator are not provided");
            throw new AppException("Credentials to tenant operator are not provided");
        }
        logger.debug("Environment variable {} = {}", ENV_TENANT_OPERATOR_CREDENTIALS, tenantOpCredentialsEnv);
        return tenantOpCredentialsEnv;
    }

    private String getAppEndpointFromEnv() throws AppException {
        String appEndpointEnv = getEnv(ENV_APPLICATION_ENDPOINT);
        if (appEndpointEnv == null) {
            logger.error("Application endpoint URL is not provided");
            throw new AppException("Application endpoint URL is not provided");
        }
        return appEndpointEnv;
    }

    private String getEnv(String envName) {
        String envValue = null;
        String sysEnvValue = System.getenv(envName);
        if (sysEnvValue == null) {
            String sysPropertyValue = System.getProperty(envName);
            if (sysPropertyValue != null && sysPropertyValue.trim()
                                                            .length() != 0) {
                envValue = sysPropertyValue;
            }
        } else {
            if (sysEnvValue.trim()
                           .length() != 0) {
                envValue = sysEnvValue;
            } else {
                envValue = null;
            }
        }
        return envValue;
    }

    public String getIasUrl() {
        return iasUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTenantOpClientId() {
        return tenantOpClientId;
    }

    public String getTenantOpIssuer() {
        return tenantOpIssuer;
    }

    public String getAppEndpoint() {
        return appEndpoint;
    }

    public String getKey() {
        return key;
    }
}
