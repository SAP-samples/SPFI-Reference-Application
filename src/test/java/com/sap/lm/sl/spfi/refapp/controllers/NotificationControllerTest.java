package com.sap.lm.sl.spfi.refapp.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.sap.lm.sl.spfi.refapp.mocks.MockControllerConstants;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;


@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void notifyTenantActivationTest() throws Exception {
        mockMvc.perform(buildPostNotifyRequest("acfbd288-bf60-4fde-af95-91efabeffe0a"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void notifyTenantTerminationTest() throws Exception {
        mockMvc.perform(buildPostNotifyRequest("tcfbd288-bf60-4fde-af95-91efabeffe0a"))
               .andExpect(status().isNoContent());
    }

    @Test
    public void notifyTenantUpdateTest() throws Exception {
        mockMvc.perform(buildPostNotifyRequest("ucfbd288-bf60-4fde-af95-91efabeffe0a"))
        .andExpect(status().isNoContent());

        this.mockMvc.perform(get(MockControllerConstants.TENANT_PATH.replace("{tenantId}", "crm-tenant-id")))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.sapCrmId", is("crm-tenant-id")));
    }


    private MockHttpServletRequestBuilder buildPostNotifyRequest(String notificationId) throws Exception {
        return post(ControllersConstants.NOTIFY_PATH).
            with(jwt()).
            header(ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, notificationId).
            header("X-CorrelationID", "123");
    }

    public void getTenant(String tenantId) throws Exception {
        this.mockMvc.perform(get(MockControllerConstants.TENANT_PATH.replace("{tenantId}", tenantId)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.sapCrmId", is(tenantId)));
    }
}
