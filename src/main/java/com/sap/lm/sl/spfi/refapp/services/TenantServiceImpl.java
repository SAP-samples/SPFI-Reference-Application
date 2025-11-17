package com.sap.lm.sl.spfi.refapp.services;

import com.sap.lm.sl.spfi.operations.client.model.*;
import com.sap.lm.sl.spfi.refapp.AppConfig;
import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import com.sap.lm.sl.spfi.refapp.controllers.NotFoundException;
import com.sap.lm.sl.spfi.refapp.mocks.IntegrationTests;
import com.sap.lm.sl.spfi.refapp.persistence.FileTenantRepository;
import com.sap.lm.sl.spfi.refapp.persistence.ITenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class TenantServiceImpl implements TenantService{
    private static final String CONFIGURATION_URL_PATTERN = "{appEndpoint}/tenants/{tenantId}";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ITenantRepository tenantRepository;
    private AppConfig appConfig;
    private final IntegrationTests integrationTests;

    public TenantServiceImpl() {
        tenantRepository = new FileTenantRepository();
        integrationTests = new IntegrationTests();
    }

    @Override
    public AppTenant activate(String xSapSpfiNotifcationId,String xSapSpfiCallbackUrl, String sapCrmTenantId, ActivateContext activateContext) throws AppException {
        logger.debug("Starting tenant activation, xSapSpfiNotifcationId: {}, CRM-tenant-ID: {}", xSapSpfiNotifcationId, sapCrmTenantId);

        // for test
        if (sapCrmTenantId.endsWith(IntegrationTests.ACTIVATION_FAIL_TENANT_ID_SUFFIX) || integrationTests.isActivationFailTestTenant(xSapSpfiNotifcationId, xSapSpfiCallbackUrl)) {
            throw new AppException("Test tenant activation failure");
        }

        if (appConfig == null) {
            appConfig = new AppConfig();
        }

        AppTenant tenant = new AppTenant();
        tenant.setSapSpfiNotifcationId(xSapSpfiNotifcationId);
        tenant.setSapCrmId(sapCrmTenantId);
        tenant.setSolutionId(activateContext.getTenant().getSolutionId());
        tenant.setSpec(activateContext.getTenant().getSpec());
        tenant.setBlocked(false);
        tenant.setServiceInstanceId("serviceinstance-id-123");
        tenant.setSubscriptionId("subscription-id-123");
        tenant.setBtpSubaccountId("btpsubaccount-id-123");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        Endpoint appEndpoint = new Endpoint();
        appEndpoint.setType(com.sap.lm.sl.spfi.operations.client.model.Endpoint.TypeEnum.APPLICATION);
        appEndpoint.setUrl(appConfig.getAppEndpoint());
        endpoints.add(appEndpoint);

        Endpoint configEndpoint = new Endpoint();
        configEndpoint.setType(com.sap.lm.sl.spfi.operations.client.model.Endpoint.TypeEnum.CONFIGURATION);
        configEndpoint.setUrl(CONFIGURATION_URL_PATTERN.replace("{appEndpoint}", appConfig.getAppEndpoint())
                                                       .replace("{tenantId}", sapCrmTenantId));
        endpoints.add(configEndpoint);
        tenant.setEndpoints(endpoints);
        tenant.setCustomer(activateContext.getCustomer());
        tenant.setinitialUsers(activateContext.getInitialUsers());
        tenantRepository.storeTenant(tenant);
        return tenant;
    }

    @Override
    public AppTenant terminate(String xSapSpfiNotifcationId, String xSapSpfiCallbackUrl, String sapCrmTenantId, TerminateContext terminateContext) throws NotFoundException, AppException {
        logger.debug("Starting tenant termination, xSapSpfiNotifcationId: {}, CRM-tenant-ID: {}", xSapSpfiNotifcationId, sapCrmTenantId);

        // for test
        if (sapCrmTenantId.endsWith(IntegrationTests.TERMINATION_FAIL_TENANT_ID_SUFFIX)) {
            throw new AppException("Test tenant termination failure");
        }

        AppTenant tenant = getTenant(sapCrmTenantId);
        tenantRepository.deleteTenantByCrmTenantId(sapCrmTenantId);
        return tenant;
    }

    @Override
    public AppTenant block(String xSapSaasfulfillmentNotifcationId, String sapCrmTenantId, BlockContext details) throws AppException {
        logger.debug("Starting tenant blocking, xSapSpfiNotifcationId: {}, Tenant-External-ID: {}, Tenant-Solution-ID: {}", 
            xSapSaasfulfillmentNotifcationId, details.getTenant().getExternalId(), details.getTenant().getSolutionId());
        AppTenant tenant = getTenant(sapCrmTenantId);
        tenant.setBlocked(true);
        tenantRepository.storeTenant(tenant);
        return tenant;
    }

    @Override
    public AppTenant setupSso(String xSapSpfiNotifcationId, String sapCrmTenantId, SsosetupContext ssosetupContext) throws AppException {
        logger.debug("Starting SSO setup, xSapSpfiNotifcationId: {}, CRM-tenant-ID: {}", xSapSpfiNotifcationId, sapCrmTenantId);
        AppTenant tenant = getTenant(sapCrmTenantId);

        //TODO
        SsosetupApplicationProvider ssosetupApplicationProvider = new SsosetupApplicationProvider();
        ssosetupApplicationProvider.setMetadataURL("https://uniproiastest.accounts400.ondemand.com/.well-known/openid-configuration");
        ssosetupApplicationProvider.setSsoType("saml2");
        ssosetupApplicationProvider.setConfigName("Config1");
        ssosetupApplicationProvider.setStatus("active");

        tenant.setSsosetupApplicationProvider(ssosetupApplicationProvider);
        tenantRepository.storeTenant(tenant);
        return tenant;
    }

    @Override
    public AppTenant getTenant(String sapCrmTenantId) throws NotFoundException, AppException {
        return tenantRepository.readTenantByCrmTenantId(sapCrmTenantId);
    }


    @Override
    public Map<String, AppTenant> getTenants() throws AppException {
        return tenantRepository.readTenants();
    }

    @Override
    public List<String> listTenants() throws AppException {
        return tenantRepository.listTenants();
    }

    @SuppressWarnings("unused")
    private void sleepSec(long timeout) {
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            System.out.println("Error in delay");
        }
    }

}
