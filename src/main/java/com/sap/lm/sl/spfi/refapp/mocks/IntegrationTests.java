package com.sap.lm.sl.spfi.refapp.mocks;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.lm.sl.spfi.refapp.AppConfig;

@Component
public class IntegrationTests {

    public static final String ACTIVATION_FAIL_TENANT_ID_SUFFIX = "-activation-fail";      // Updates status of tenant activation with subject message "Tenant activation failed: Test tenant activation failure"
    public static final String TERMINATION_FAIL_TENANT_ID_SUFFIX = "-termination-fail";    // Updates status of tenant termination with subject message "Tenant termination failed: Test tenant termination failure"
    public static final String NOTIFICATION_FAIL_TENANT_ID_SUFFIX = "-notification-fail";  // Notification call responses with HTTP status 400 and body {"error":{"message":"Notification failure test","code":"020"}}
    public static final String TENANT_NOTIFICATION_FAIL_PATH_PREFIX = "/clm/unified-provisioning/sandbox/integration/notification-fail";  // Notification call responses with HTTP status 400 and body {"error":{"message":"Notification failure test","code":"020"}}
    public static final String TENANT_ACTIVATION_FAIL_PATH_PREFIX = "/clm/unified-provisioning/sandbox/integration/activation-fail";  // Updates status of tenant activation with subject message "Tenant activation failed: Test tenant activation failure"
    public static final String MOCK_RESOLVE_FAIL_NOTIFICATION_ID_SUFFIX = "-resolve-fail";
    public static final String NOTIFICATION_REASON_ACTIVATE = "Activate";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AppConfig appConfig;
    public IntegrationTests() {
        appConfig = new AppConfig();
    }

    public String getNotificationJson(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl) throws Exception {
        if (xSapSaasfulfillmentNotifcationId == null || xSapSaasfulfillmentCallbackUrl == null) {
            throw new Exception("Invalid notificationId or callback url");
        }
        String fullKey = appConfig.getKey();
        if (fullKey == null) {
            throw new Exception("No notification ID key provided");
        }
        String key = fullKey.split("\\.")[0];
        String iv = fullKey.split("\\.")[1];

        try {
            return DecodeNotificationId.decode(xSapSaasfulfillmentNotifcationId, key, iv);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Exception(e.getMessage());
        }
    }

    public boolean isTestTenantByName(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl) {
        String tenantId = null;
        try {
            tenantId = getTenantId(getNotificationJson(xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl));
        } catch (Exception e) {
            logger.error("could not get notification json" + e.getMessage(), e);
        }
        return (tenantId!=null && tenantId.endsWith(NOTIFICATION_FAIL_TENANT_ID_SUFFIX));
    }

    public boolean isTestTenantByPath(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl, String prefixPath) {
        String path = null;
        try {
            path = getPath(getNotificationJson(xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl));
        } catch (Exception e) {
            logger.error("could not get notification json" + e.getMessage(), e);
        }
        return (path!=null && path.startsWith(prefixPath));
    }

    public boolean isNotificationFailTestTenant(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl) {
        return isTestTenantByPath(xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl, TENANT_NOTIFICATION_FAIL_PATH_PREFIX);
    }

    public boolean isActivationFailTestTenant(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl) {
        return isTestTenantByPath(xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl, TENANT_ACTIVATION_FAIL_PATH_PREFIX);
    }

    public boolean isActivation(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl) {
        if (xSapSaasfulfillmentNotifcationId == null || xSapSaasfulfillmentCallbackUrl == null) {
            return false;
        }
        String fullKey = appConfig.getKey();
        if (fullKey == null) {
            logger.debug("No notification ID key provided");
            return false;
        }
        String key = fullKey.split("\\.")[0];
        String iv = fullKey.split("\\.")[1];
        
        String reason = null;
        try {
            reason = getReason(DecodeNotificationId.decode(xSapSaasfulfillmentNotifcationId, key, iv));
        } catch (Exception e) {
            logger.error("could not get notification json" + e.getMessage(), e);
        }

        return (NOTIFICATION_REASON_ACTIVATE.equals(reason));
    }

    private String getTenantId(String notificationJson) {  // resource-name
        String tenantId = null;
        try {
            JSONObject notification = new JSONObject(notificationJson);
            JSONObject resource = notification.getJSONObject("resource");
            tenantId = resource.getString("name");
            logger.debug("Tenant ID from the Notification ID: {}", tenantId);
        } catch (JSONException e) {
            logger.error("Could not parse Notification ID. " + e.getMessage(), e);
        }
        return tenantId;
    }

    private String getReason(String notificationJson) {  // reason
        String reason = null;
        try {
            JSONObject notification = new JSONObject(notificationJson);
            reason = notification.getString("reason");
            logger.debug("Reason from the Notification ID: {}", reason);
        } catch (JSONException e) {
            logger.error("Could not parse Notification ID. " + e.getMessage(), e);
        }
        return reason;
    }

    private String getPath(String notificationJson) {  // path
        String path = null;
        try {
            JSONObject notification = new JSONObject(notificationJson);
            JSONObject resource = notification.getJSONObject("resource");
            path = resource.getString("path");
            logger.debug("Path from the Notification ID: {}", path);
        } catch (JSONException e) {
            logger.error("Could not parse Notification ID. " + e.getMessage(), e);
        }
        return path;
    }

}
