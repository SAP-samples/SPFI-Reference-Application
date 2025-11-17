package com.sap.lm.sl.spfi.refapp.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.lm.sl.spfi.operations.client.ClientCredentials;
import com.sap.lm.sl.spfi.operations.client.ISpfiOperationsClient;
import com.sap.lm.sl.spfi.operations.client.SpfiClientException;
import com.sap.lm.sl.spfi.operations.client.SpfiOperationsClientFactory;
import com.sap.lm.sl.spfi.operations.client.model.Activate;
import com.sap.lm.sl.spfi.operations.client.model.ActivateContext;
import com.sap.lm.sl.spfi.operations.client.model.AdditionalProperty;
import com.sap.lm.sl.spfi.operations.client.model.Block;
import com.sap.lm.sl.spfi.operations.client.model.BlockContext;
import com.sap.lm.sl.spfi.operations.client.model.OneOfstatusDetails;
import com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillment;
import com.sap.lm.sl.spfi.operations.client.model.Ssosetup;
import com.sap.lm.sl.spfi.operations.client.model.SsosetupApplicationProvider;
import com.sap.lm.sl.spfi.operations.client.model.SsosetupContext;
import com.sap.lm.sl.spfi.operations.client.model.Status;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject;
import com.sap.lm.sl.spfi.operations.client.model.Terminate;
import com.sap.lm.sl.spfi.operations.client.model.TerminateContext;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.CommandEnum;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.StatusEnum;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.TypeEnum;
import com.sap.lm.sl.spfi.refapp.AppConfig;
import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import com.sap.lm.sl.spfi.refapp.controllers.NotFoundException;

@Component
public class SpfiOperatorServiceImpl implements SpfiOperatorService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ISpfiOperationsClient spfiOperationsClient;

    @Inject
    AppConfig appConfig;

    @Override
    public void processFulfillment(String xSapSaasfulfillmentNotifcationId, String xSapSaasfulfillmentCallbackUrl) throws AppException {
        spfiOperationsClient = getSpfiOperationsClient(xSapSaasfulfillmentCallbackUrl);
        SaaSFulfillment saaSFulfillmentRequest = resolveFulfillment(xSapSaasfulfillmentNotifcationId);
        String subjectId = saaSFulfillmentRequest.getSubject().getId();

        switch (saaSFulfillmentRequest.getSubject()
                               .getType()) {
            case TENANT:
                TenantService tenantService = new TenantServiceImpl();
                AppTenant tenant = null;
                StatusEnum operationStatus = null;
                String subjectMessage = "Success";
                String subjectDisplayMessage = saaSFulfillmentRequest.getSubject().getCommand() + " command executed successfully";
                switch (saaSFulfillmentRequest.getSubject()
                                       .getCommand()) {
                    case ACTIVATE:
                        try {
                            tenant = tenantService.activate(xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl, subjectId, (ActivateContext) saaSFulfillmentRequest.getDetails());
                            operationStatus=StatusEnum.COMPLETED;
                        } catch (Exception e) {
                            subjectMessage = "Tenant activation failed: " + e.getMessage();
                            subjectDisplayMessage = "Tenant activation failed due to some error";
                            logger.error(subjectMessage, e);
                            operationStatus=StatusEnum.ERROR;
                        }
                        updateTenantActivationStatus(xSapSaasfulfillmentNotifcationId, subjectId, tenant, operationStatus, subjectMessage, subjectDisplayMessage);
                        break;

                    case TERMINATE:
                        try {
                            tenant = tenantService.terminate(xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl, subjectId, (TerminateContext) saaSFulfillmentRequest.getDetails());
                            operationStatus=StatusEnum.COMPLETED;
                        } catch (NotFoundException e) {
                            subjectMessage = e.getMessage() + ". Tenant termination completed";
                            logger.info(subjectMessage);
                            operationStatus=StatusEnum.COMPLETED;
                        } catch (Exception e) {
                            subjectMessage = "Tenant termination failed: " + e.getMessage();
                            subjectDisplayMessage = "Tenant termination failed due to some error";
                            logger.error(subjectMessage, e);
                            operationStatus=StatusEnum.ERROR;
                        }
                        updateTenantTerminationStatus(xSapSaasfulfillmentNotifcationId, subjectId, tenant, operationStatus, subjectMessage, subjectDisplayMessage);
                        break;

                    case BLOCK:
                        try {
                            tenant = tenantService.block(xSapSaasfulfillmentNotifcationId, subjectId, (BlockContext) saaSFulfillmentRequest.getDetails());
                            operationStatus=StatusEnum.COMPLETED;
                        } catch (Exception e) {
                            subjectMessage = "Block command failed: " + e.getMessage();
                            subjectDisplayMessage = "Block command failed due to some error";
                            logger.error(subjectMessage, e);
                            operationStatus = StatusEnum.ERROR;
                        }
                        updateTenantBlockingStatus(xSapSaasfulfillmentNotifcationId, subjectId, tenant, operationStatus, subjectMessage, subjectDisplayMessage);
                        break;

                    case SSOSETUP:
                        try {
                            tenant = tenantService.setupSso(xSapSaasfulfillmentNotifcationId, subjectId, (SsosetupContext) saaSFulfillmentRequest.getDetails());
                            operationStatus=StatusEnum.COMPLETED;
                        } catch (Exception e) {
                            subjectMessage = "SSO setup failed: " + e.getMessage();
                            subjectDisplayMessage = "SSO setup failed due to some error";
                            logger.error(subjectMessage, e);
                            operationStatus = StatusEnum.ERROR;
                        }
                        updateSsoSetupStatus(xSapSaasfulfillmentNotifcationId, subjectId, tenant, operationStatus, subjectMessage, subjectDisplayMessage);
                        break;

                    default:
                        break;
                }
                break;
        }
    }

    private SaaSFulfillment resolveFulfillment(String notifcationId) throws AppException {
        SaaSFulfillment saaSFulfillment = null;
        try {
            saaSFulfillment = spfiOperationsClient.resolvetFulfillment(notifcationId);
        } catch (SpfiClientException e) {
            logger.error("Could not resolve fulfillment", e);
            throw new AppException("Could not resolve fulfillment: " + e.getMessage());
        }
        return saaSFulfillment;
    }

    private void updateTenantActivationStatus(String notifcationId, String crmTenantId, AppTenant tenant, StatusEnum statusStatus, String message, String displayMessage) {
        logger.debug("Updating tenant activation status for notification ID {}, CRM-Tenant-ID {}", notifcationId.toString(), crmTenantId);
        Status status = new Status();
        StatusSubject statusSubject = new StatusSubject();
        statusSubject.setId(crmTenantId);
        statusSubject.setType(TypeEnum.TENANT);
        statusSubject.setCommand(CommandEnum.ACTIVATE);
        statusSubject.setStatus(statusStatus);
        statusSubject.setMessage(message);
        //statusSubject.setDisplayMessage(displayMessage);
        status.setSubject(statusSubject);

        Activate activate = new Activate();
        if (tenant != null) {
            activate.setExternalId(tenant.getId());
            activate.setEndpoints(tenant.getEndpoints());

            Map<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put("serviceInstanceId", tenant.getServiceInstanceId());
            additionalProperties.put("subscriptionId", tenant.getSubscriptionId());
            additionalProperties.put("btpSubaccountId", tenant.getBtpSubaccountId());
            activate.setAdditionalProperties(additionalProperties);
            status.setDetails((OneOfstatusDetails) activate);
        }

//        AdditionalProperty serviceInstanceId = new AdditionalProperty();
//        serviceInstanceId.put("serviceinstanceid", tenant.getServiceInstanceId());
//        additionalProperties.add(serviceInstanceId);
//
//        AdditionalProperty subscriptionId = new AdditionalProperty();
//        subscriptionId.put("subscriptionid", tenant.getSubscriptionId());
//        additionalProperties.add(subscriptionId);
//
//        AdditionalProperty btpSubaccountId = new AdditionalProperty();
//        btpSubaccountId.put("btpsubaccount-id-123", tenant.getBtpSubaccountId());
//        additionalProperties.add(btpSubaccountId);

        
        updateStatus(notifcationId, status);
    }

    private void updateTenantTerminationStatus(String notifcationId, String crmTenantId, AppTenant tenant, StatusEnum statusStatus, String message, String displayMessage) {
        logger.debug("Updating tenant termination status for notification ID {}, CRM-tenant-ID {}", notifcationId.toString(), crmTenantId);
        Status status = new Status();
        StatusSubject statusSubject = new StatusSubject();
        statusSubject.setId(crmTenantId);
        statusSubject.setType(TypeEnum.TENANT);
        statusSubject.setCommand(CommandEnum.TERMINATE);
        statusSubject.setStatus(statusStatus);
        statusSubject.setMessage(message);
        //statusSubject.setDisplayMessage(displayMessage);
        status.setSubject(statusSubject);

        Terminate terminate = new Terminate();
        if (tenant != null) {
            terminate.setExternalId(tenant.getId());
        }
        status.setDetails((OneOfstatusDetails) terminate);
        updateStatus(notifcationId, status);
    }

    private void updateTenantBlockingStatus(String notifcationId, String crmTenantId, AppTenant tenant, StatusEnum statusStatus, String message, String displayMessage) {
        logger.debug("Updating tenant blocking status for notification ID {}, CRM-tenant-ID {}", notifcationId.toString(), crmTenantId);
        Status status = new Status();
        StatusSubject statusSubject = new StatusSubject();
        statusSubject.setId(crmTenantId);
        statusSubject.setType(TypeEnum.TENANT);
        statusSubject.setCommand(CommandEnum.BLOCK);
        statusSubject.setStatus(statusStatus);
        statusSubject.setMessage(message);
        statusSubject.setDisplayMessage(displayMessage);
        status.setSubject(statusSubject);

        Block block = new Block();
        if (tenant != null) {
            block.setExternalId(tenant.getId());
            Map<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put("serviceInstanceId", tenant.getServiceInstanceId());
            additionalProperties.put("subscriptionId", tenant.getSubscriptionId());
            additionalProperties.put("btpSubaccountId", tenant.getBtpSubaccountId());
            block.setAdditionalProperties(additionalProperties);
            status.setDetails((OneOfstatusDetails) block);
        }

        updateStatus(notifcationId, status);
    }

    private void updateSsoSetupStatus(String sapSpfiNotifcationId, String crmTenantId, AppTenant tenant, StatusEnum statusStatus, String message, String displayMessage) {
        logger.debug("Updating SSO setup status for notification ID {}, CRM-tenant-ID {}", sapSpfiNotifcationId.toString(), crmTenantId);
        Status status = new Status();
        StatusSubject statusSubject = new StatusSubject();
        statusSubject.setId(crmTenantId);
        statusSubject.setType(TypeEnum.TENANT);
        statusSubject.setCommand(CommandEnum.SSOSETUP);
        statusSubject.setStatus(statusStatus);
        statusSubject.setMessage(message);
        statusSubject.setDisplayMessage(displayMessage);
        status.setSubject(statusSubject);

        Ssosetup ssosetup = new Ssosetup();
        if (tenant != null) {
            SsosetupApplicationProvider ssosetupApplicationProvider = tenant.getSsosetupApplicationProvider();
            ssosetup.setApplicationProvider(ssosetupApplicationProvider);
        }
        status.setDetails((OneOfstatusDetails) ssosetup);
        updateStatus(sapSpfiNotifcationId, status);
    }

    private void updateStatus(String xSapSpfiNotifcationId, Status status) {
        try {
            spfiOperationsClient.updateFulfillment(xSapSpfiNotifcationId, status);
        } catch (SpfiClientException e) {
            logger.error("Could not update status", e);
            //throw new AppException("Could not update status: " + e.getMessage());
        }
    }

    private ISpfiOperationsClient getSpfiOperationsClient(String xSapSaasfulfillmentCallbackUrl) throws AppException {
        String callbackUrl = "";
        if (xSapSaasfulfillmentCallbackUrl != null) {
            callbackUrl = xSapSaasfulfillmentCallbackUrl;
            if (callbackUrl.endsWith("/")) {
                callbackUrl = callbackUrl.substring(0, callbackUrl.length() - 1);
            }
        }

        String iasUrl = appConfig.getIasUrl();
        String clientId = appConfig.getClientId();
        String clientSecret = appConfig.getClientSecret();

//        logger.debug("iasUrl: " + iasUrl);
//        logger.debug("clientId: " + clientId);
//        logger.debug("clientSecret: " + clientSecret);
        logger.debug("SAAS Fulfillment Callback URL: " + callbackUrl);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        ISpfiOperationsClient spfiOperationsClient = SpfiOperationsClientFactory.getInstance()
                                                                                .createSpfiOperationsClient(httpClient, callbackUrl,
                                                                                    new ClientCredentials(iasUrl, clientId, clientSecret));
        return spfiOperationsClient;
    }
}
