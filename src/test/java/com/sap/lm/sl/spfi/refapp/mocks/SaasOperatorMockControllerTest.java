package com.sap.lm.sl.spfi.refapp.mocks;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.lm.sl.spfi.operations.client.model.Activate;
import com.sap.lm.sl.spfi.operations.client.model.Endpoint;
import com.sap.lm.sl.spfi.operations.client.model.OneOfstatusDetails;
import com.sap.lm.sl.spfi.operations.client.model.Status;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.CommandEnum;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.StatusEnum;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.TypeEnum;
import com.sap.lm.sl.spfi.refapp.controllers.ControllersConstants;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
public class SaasOperatorMockControllerTest {
    
    @Autowired
    private MockMvc mockMvc;


    @Test
    public void test() throws Exception {

    }

    @Test
    public void resolveTenantActivationTest() throws Exception {
        mockMvc.perform(buildGetResolveRequest("acfbd288-bf60-4fde-af95-91efabeffe0a"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.subject.type", is("tenant")))
               .andExpect(jsonPath("$.subject.command", is("activate")))
               .andExpect(jsonPath("$.details.tenant.solutionId", is("atom-spfi-application-uuid")))
               .andExpect(jsonPath("$.details.tenant.spec.iasUrl", is("https://demo.accounts400.ondemand.com")))
               .andExpect(jsonPath("$.details.customer.id", is("customer-1234")))
               .andExpect(jsonPath("$.details.tenant.spec.dataCenter", is("cf-eu10")))
               .andExpect(jsonPath("$.details.tenant.spec.spcDataCenter", is("XAF")))
               .andExpect(jsonPath("$.details.tenant.operationType", is("9505372")))
               .andExpect(jsonPath("$.details.tenant.spec.skus[0]", is("SKU12345")))
               .andExpect(jsonPath("$.details.tenant.spec.products[1].productId", is("SKU67890")));

    }

    @Test
    public void resolveTenantTerminationTest() throws Exception {
        mockMvc.perform(buildGetResolveRequest("tcfbd288-bf60-4fde-af95-91efabeffe0a"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.subject.type", is("tenant")))
               .andExpect(jsonPath("$.subject.command", is("terminate")))
               .andExpect(jsonPath("$.details.tenant.solutionId", is("atom-spfi-application-uuid")))
                .andExpect(jsonPath("$.details.tenant.spec.dataCenter", is("cf-eu10")))
                .andExpect(jsonPath("$.details.tenant.spec.spcDataCenter", is("XAF")));
    }

    @Test
    public void resolveTenantBlockTest() throws Exception {
        mockMvc.perform(buildGetResolveRequest("bcfbd288-bf60-4fde-af95-91efabeffe0a"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.subject.type", is("tenant")))
                .andExpect(jsonPath("$.subject.command", is("block")))
                .andExpect(jsonPath("$.details.tenant.solutionId", is("atom-spfi-application-uuid")))
                .andExpect(jsonPath("$.details.tenant.spec.dataCenter", is("cf-eu10")))
                .andExpect(jsonPath("$.details.tenant.spec.spcDataCenter", is("XAF")));
    }

    @Test
    public void updateStatusTest() throws Exception {
        Status status = new Status();
        StatusSubject statusSubject = new StatusSubject();
        statusSubject.setType(TypeEnum.TENANT);
        statusSubject.setCommand(CommandEnum.ACTIVATE);
        statusSubject.setStatus(StatusEnum.COMPLETED);
        status.setSubject(statusSubject);

        Activate activate = new Activate();
        activate.setExternalId("external-tenant-id");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        Endpoint appEndpoint = new Endpoint();
        appEndpoint.setType(com.sap.lm.sl.spfi.operations.client.model.Endpoint.TypeEnum.APPLICATION);
        appEndpoint.setUrl("https://tenant.co.com");
        endpoints.add(appEndpoint);

        Endpoint configEndpoint = new Endpoint();
        configEndpoint.setType(com.sap.lm.sl.spfi.operations.client.model.Endpoint.TypeEnum.CONFIGURATION);
        configEndpoint.setUrl("https://tenant.configuration.co.com");
        endpoints.add(configEndpoint);

        activate.setEndpoints(endpoints);

        status.setDetails((OneOfstatusDetails) activate);

        mockMvc.perform(buildPostUpdateStatusRequest("5cfbd288-bf60-4fde-af95-91efabeffe0a", status))
        .andExpect(status().isOk());
    }


    private MockHttpServletRequestBuilder buildGetResolveRequest(String notificationId) throws Exception {
        return get(MockControllerConstants.SAAS_OPERATOR_RESOLVE_ABS_PATH).header(ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, notificationId);
    }

    private MockHttpServletRequestBuilder buildPostUpdateStatusRequest(String notificationId, Status status) throws Exception {
        return post(MockControllerConstants.SAAS_OPERATOR_UPDATE_STATUS_ABS_PATH).content(toJson(status)).contentType(APPLICATION_JSON).header(ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, notificationId);
    }

    private String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}
