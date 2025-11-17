package com.sap.lm.sl.spfi.operations.client;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sap.lm.sl.spfi.operations.client.model.ActivateContext;
import com.sap.lm.sl.spfi.operations.client.model.BlockContext;
import com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillment;
import com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject;
import com.sap.lm.sl.spfi.operations.client.model.SsosetupContext;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.CommandEnum;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.StatusEnum;
import com.sap.lm.sl.spfi.operations.client.model.Status;
import com.sap.lm.sl.spfi.refapp.controllers.ControllersConstants;
import com.sap.lm.sl.spfi.operations.client.model.TerminateContext;

public class SpfiOperationsClient implements ISpfiOperationsClient {

    private static final String MSG_NOTIFICATION_ID_CAN_T_BE_NULL = "xSapSpfiNotifcationId can't be null";
    private String baseUrl;
    private ObjectMapper objectMapper;
    private RestHelper restHelper;
    private TokenProvider tokenProvider;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SpfiOperationsClient() {
        objectMapper = new ObjectMapper();
    }

    public SpfiOperationsClient(HttpClient httpClient, String baseUrl, ClientCredentials clientCredentials) {
        this.baseUrl = baseUrl;
        restHelper = new RestHelper(httpClient);
        objectMapper = new ObjectMapper();
        tokenProvider = new TokenProvider(clientCredentials);
    }

    @Override
    public SaaSFulfillment resolvetFulfillment(String notifcationId) throws SpfiClientException {
        String operation = SpfiClientConstants.OP_RESOLVE_FULFILLMENT;
        if (notifcationId == null) {
            restHelper.handleArgumentValidationException(MSG_NOTIFICATION_ID_CAN_T_BE_NULL);
        }
        logger.debug("Resolving fulfillment for NotifcationId {}", notifcationId);
        String url = baseUrl + SpfiClientConstants.RESOLVE_FULFILLMENT_URL;
        //restHelper.validateUrl(operation, url);
        restHelper.traceUrl(operation, url);

        Header[] headers = new BasicHeader[2];

        headers[0] = buildAuthorizationHeader();

        BasicHeader headerNotificationId = new BasicHeader(ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, notifcationId.toString());
        headers[1] = headerNotificationId;

        String response = restHelper.doHttpGet(url, headers, operation);

        SaaSFulfillment saaSFulfillment = new SaaSFulfillment();
        SaaSFulfillmentSubject saaSFulfillmentSubject = null;
        JsonObject fulfillmentJson = null;
        try {
            fulfillmentJson = new Gson().fromJson(response, JsonObject.class);
            JsonElement subjectJson = fulfillmentJson.get("subject");
            saaSFulfillmentSubject = new Gson().fromJson(subjectJson, SaaSFulfillmentSubject.class);
            saaSFulfillment.setSubject(saaSFulfillmentSubject);

            com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.CommandEnum command = saaSFulfillmentSubject.getCommand();
            JsonElement detailsJson = fulfillmentJson.get("details");
            switch (command) {
                case ACTIVATE:
                    ActivateContext activateContext = objectMapper.readValue(detailsJson.toString(), ActivateContext.class);
                    saaSFulfillment = saaSFulfillment.details(activateContext);
                    logger.info("Successfully resolved request for tenant activation with xSapSpfiNotifcationId: {}", notifcationId);
                    break;
                case TERMINATE:
                    TerminateContext terminateContext = objectMapper.readValue(detailsJson.toString(), TerminateContext.class);
                    saaSFulfillment = saaSFulfillment.details(terminateContext);
                    logger.info("Successfully resolved request for tenant termination with xSapSpfiNotifcationId: {}", notifcationId);
                    break;
                case BLOCK:
                    BlockContext blockContext = objectMapper.readValue(detailsJson.toString(), BlockContext.class);
                    saaSFulfillment = saaSFulfillment.details(blockContext);
                    logger.info("Successfully resolved request for tenant blocking with xSapSpfiNotifcationId: {}", notifcationId);
                    break;
                case SSOSETUP:
                    SsosetupContext ssosetupContext = objectMapper.readValue(detailsJson.toString(), SsosetupContext.class);
                    saaSFulfillment = saaSFulfillment.details(ssosetupContext);
                    logger.info("Successfully resolved request for SSO setup with xSapSpfiNotifcationId: {}", notifcationId);
                    break;
                default:
                    break;
            }
        } catch (JSONException | IOException e) {
            logger.error(MessageFormat.format(SpfiClientMessages.SERVER_RESPONSE, response));
            restHelper.handleReportableException(MessageFormat.format(SpfiClientMessages.DESERIALIZATION_ERROR, operation), e);
        }
        return saaSFulfillment;
    }

    @Override
    public void updateFulfillment(String xSapSpfiNotifcationId, Status status) throws SpfiClientException {
        String operation = SpfiClientConstants.OP_UPDATE_STATUS;
        CommandEnum command = status.getSubject().getCommand();
        StatusEnum status1 = status.getSubject().getStatus();
        logger.info("Updating status of the command {} as {}, xSapSpfiNotifcationId: {}", command, status1, xSapSpfiNotifcationId);

        String url = baseUrl + SpfiClientConstants.UPDATE_STATUS_URL;
        //restHelper.validateUrl(operation, url);
        restHelper.traceUrl(operation, url);

        Header[] headers = new BasicHeader[2];

        headers[0] = buildAuthorizationHeader();

        BasicHeader header = new BasicHeader(ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, xSapSpfiNotifcationId.toString());
        headers[1] = header;
        restHelper.doHttpPost(url, status, headers, operation);
    }

    private BasicHeader buildAuthorizationHeader() throws SpfiClientException {
        String token = tokenProvider.retrieveToken();
        BasicHeader headerAuthorization = new BasicHeader(SpfiClientConstants.HEADER_AUTHORIZATION, SpfiClientConstants.BEARER_TOKEN_TYPE + token);
        return headerAuthorization;
    }

}
