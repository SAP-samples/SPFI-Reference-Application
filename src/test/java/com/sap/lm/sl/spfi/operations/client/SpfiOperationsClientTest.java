package com.sap.lm.sl.spfi.operations.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.lm.sl.spfi.operations.client.model.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.StatusEnum;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.TypeEnum;
import com.sap.lm.sl.spfi.operations.client.model.StatusSubject.CommandEnum;


public class SpfiOperationsClientTest {

    private static final String TEST_SPFI_NOTIFICATION_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    private static final String RESOLVE_FULFILLMENT_TENANT_ACTIVATE_RESPONSE = "{"
        + "  \"subject\": {"
        + "    \"type\": \"tenant\","
        + "    \"id\": \"tenant-id-1234\","
        + "    \"command\": \"activate\""
        + "  },"
        + "  \"details\": {"
        + "    \"tenant\": {"
        + "      \"solutionId\": \"atom-spfi-application-uuid\","
        + "      \"operationType\": \"9505372\","
        + "      \"spec\": {"
        + "        \"region\": \"eu\","
        + "        \"dataCenter\": \"cf-eu10\","
        + "        \"spcDataCenter\": \"XAF\","
        + "        \"businessType\": \"ZH726\","
        + "         \"skus\": ["
        + "             \"SKU12345\","
        + "             \"SKU67890\""
        + "             ],"
        + "         \"products\": ["
        + "             {"
        + "                 \"productId\": \"SKU12345\","
        + "                 \"quantity\": 100,"
        + "                 \"unit\": \"GB\""
        + "             },"
        + "             {"
        + "                 \"productId\": \"SKU67890\","
        + "                 \"quantity\": 50,"
        + "                 \"unit\": \"TB\""
        + "             }"
        + "         ]"
        + "      }"
        + "    },"
        + "    \"customer\": {"
        + "      \"id\": \"customer-1234\","
        + "      \"erpId\": \"0000123456\","
        + "      \"name\": \"Demo customer\","
        + "      \"contact\": {"
        + "        \"id\": \"SAPC000987\","
        + "        \"name\": \"contact-user-01\","
        + "        \"email\": \"cuser01@demo.com\","
        + "        \"phone\": \"+49 494949498589\""
        + "      }"
        + "    },"
        + "    \"initialUsers\": ["
        + "      {"
        + "        \"id\": \"1001\","
        + "        \"name\": \"testuser01\","
        + "        \"firstName\": \"Joey\","
        + "        \"lastName\": \"Tribbiani\","
        + "        \"email\": \"testuser@demo.com\""
        + "      },"
        + "      {"
        + "        \"id\": \"1002\","
        + "        \"name\": \"contact-user-01\","
        + "        \"email\": \"cuser01@demo.com\""
        + "      }"
        + "    ],"
        + "    \"additionalProperties\": {"
        + "         \"spec.PartnerAppTestTenant\": \"{\\\"number-of-users\\\":\\\"23\\\"}\" }"
        + "   }"
        + "}";

    private static final String RESOLVE_FULFILLMENT_TENANT_TERMINATE_RESPONSE = "{"
            + "  \"subject\": {"
            + "    \"type\": \"tenant\","
            + "    \"id\": \"tenant-id-1234\","
            + "    \"command\": \"terminate\""
            + "  },"
            + "  \"details\": {"
            + "    \"tenant\": {"
            + "      \"solutionId\": \"atom-spfi-application-uuid\","
            + "      \"externalId\": \"external-id-1234\","
            + "      \"spec\": {"
            + "        \"dataCenter\": \"cf-eu10\","
            + "        \"spcDataCenter\": \"XAF\","
            + "        \"businessType\": \"ZH726\""
            + "      }"
            + "    },"
            + "    \"additionalProperties\": {"
            + "         \"spec.PartnerAppTestTenant\": \"{\\\"number-of-users\\\":\\\"23\\\"}\" }"
            + "   }"
            + "}";

    private static final String RESOLVE_FULFILLMENT_TENANT_BLOCK_RESPONSE = "{"
            + "  \"subject\": {"
            + "    \"type\": \"tenant\","
            + "    \"id\": \"tenant-id-1234\","
            + "    \"command\": \"block\""
            + "  },"
            + "  \"details\": {"
            + "    \"tenant\": {"
            + "      \"solutionId\": \"atom-spfi-application-uuid\","
            + "      \"externalId\": \"external-id-1234\","
            + "      \"spec\": {"
            + "        \"dataCenter\": \"cf-eu10\","
            + "        \"spcDataCenter\": \"XAF\","
            + "        \"businessType\": \"ZH726\""
            + "      }"
            + "    },"
            + "    \"additionalProperties\": {"
            + "         \"spec.PartnerAppTestTenant\": \"{\\\"number-of-users\\\":\\\"23\\\"}\" }"
            + "   }"
            + "}";

    private static final String RESOLVE_FULFILLMENT_SSO_SETUP_RESPONSE = "{"
        + "  \"subject\": {"
        + "    \"type\": \"tenant\","
        + "    \"id\": \"tenant-id-1234\","
        + "    \"command\": \"ssosetup\""
        + "  },"
        + "  \"details\": {"
        + "    \"tenant\": {"
        + "      \"solutionId\": \"atom-spfi-application-uuid\""
        + "    },"
        + "    \"identityProvider\": {"
        + "      \"ssoType\": \"saml2\","
        + "      \"metadataURL\": \"metadata-url\""
        + "    }"
        + "  }"
        + "}";

    private static final String GET_TOKEN_RESPONSE = "{"
        + "    \"access_token\": \"eyJqa3UiOiJodHR\","
        + "    \"token_type\": \"Bearer\","
        + "    \"expires_in\": 3600"
        + "}";
    
    private static final String SAAS_OP_SERVICE_ENDPOINT = "http://localhost:%s";
    static int port;
    private static final String TOKEN_URL = "/oauth2/token";
    private static final String APPLICATION_JSON = "application/json";
    public static final String TIMESTAMP_WEB_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; // 2017-12-27T23:45:32.999Z RFC-3339 with millisecond fractions, UTC/Zulu time

    static {
        try {
            // Get a free port
            ServerSocket s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
        } catch (IOException e) {
            System.out.println("Could not get a free HTTP port");
        }
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(port);

    @Test
    public void resolveFulfillmentTenantActivateTest() throws IOException, SpfiClientException {

        stubFor(post(urlPathMatching(TOKEN_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON)
                .withBody(GET_TOKEN_RESPONSE)));

        stubFor(get(urlPathMatching(SpfiClientConstants.RESOLVE_FULFILLMENT_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON)
                .withBody(RESOLVE_FULFILLMENT_TENANT_ACTIVATE_RESPONSE)));

        ISpfiOperationsClient spfiOperationsClient = getSpfiOperationsClient();

        SaaSFulfillment saaSFulfillmentRequest = spfiOperationsClient.resolvetFulfillment(TEST_SPFI_NOTIFICATION_ID);

        // check response
        assertNotNull(saaSFulfillmentRequest);

        SaaSFulfillmentSubject subject = saaSFulfillmentRequest.getSubject();
        assertNotNull(subject);
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.CommandEnum.ACTIVATE, subject.getCommand());
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.TypeEnum.TENANT, subject.getType());

        ActivateContext details = (ActivateContext) saaSFulfillmentRequest.getDetails();
        assertNotNull(details);
        assertNotNull(details.getTenant());
        assertEquals("atom-spfi-application-uuid", details.getTenant().getSolutionId());
        assertEquals("XAF", details.getTenant().getSpec().getSpcDataCenter());
        Map<String, Object> properties = details.getAdditionalProperties().getProperties();
        assertNotNull(properties.get("spec.PartnerAppTestTenant"));
        assertEquals("9505372", details.getTenant().getOperationType());
        assertEquals(2, details.getTenant().getSpec().getSkus().size());
        assertEquals(2, details.getTenant().getSpec().getProducts().size());
        assertEquals("0000123456", details.getCustomer().getErpId());
        assertEquals("SAPC000987", details.getCustomer().getContact().getId());
        assertEquals("1001", details.getInitialUsers().get(0).getId());
        assertEquals("Joey", details.getInitialUsers().get(0).getFirstName());
        assertEquals("Tribbiani", details.getInitialUsers().get(0).getLastName());
        assertEquals("ZH726", details.getTenant().getSpec().getBusinessType());
        // ... TODO
    }

    @Test
    public void resolveFulfillmentTenantTerminateTest() throws IOException, SpfiClientException {

        stubFor(post(urlPathMatching(TOKEN_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(GET_TOKEN_RESPONSE)));

        stubFor(get(urlPathMatching(SpfiClientConstants.RESOLVE_FULFILLMENT_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(RESOLVE_FULFILLMENT_TENANT_TERMINATE_RESPONSE)));

        ISpfiOperationsClient spfiOperationsClient = getSpfiOperationsClient();

        SaaSFulfillment saaSFulfillmentRequest = spfiOperationsClient.resolvetFulfillment(TEST_SPFI_NOTIFICATION_ID);

        // check response
        assertNotNull(saaSFulfillmentRequest);

        SaaSFulfillmentSubject subject = saaSFulfillmentRequest.getSubject();
        assertNotNull(subject);
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.CommandEnum.TERMINATE, subject.getCommand());
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.TypeEnum.TENANT, subject.getType());

        TerminateContext details = (TerminateContext) saaSFulfillmentRequest.getDetails();
        assertNotNull(details);
        assertNotNull(details.getTenant());
        assertEquals("atom-spfi-application-uuid", details.getTenant().getSolutionId());
        assertEquals("cf-eu10", details.getTenant().getSpec().getDataCenter());
        assertEquals("ZH726", details.getTenant().getSpec().getBusinessType());
        assertEquals("XAF", details.getTenant().getSpec().getSpcDataCenter());
        Map<String, Object> properties = details.getAdditionalProperties().getProperties();
        assertNotNull(properties.get("spec.PartnerAppTestTenant"));
    }

    @Test
    public void resolveFulfillmentTenantBlockTest() throws IOException, SpfiClientException {

        stubFor(post(urlPathMatching(TOKEN_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(GET_TOKEN_RESPONSE)));

        stubFor(get(urlPathMatching(SpfiClientConstants.RESOLVE_FULFILLMENT_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(RESOLVE_FULFILLMENT_TENANT_BLOCK_RESPONSE)));

        ISpfiOperationsClient spfiOperationsClient = getSpfiOperationsClient();

        SaaSFulfillment saaSFulfillmentRequest = spfiOperationsClient.resolvetFulfillment(TEST_SPFI_NOTIFICATION_ID);

        // check response
        assertNotNull(saaSFulfillmentRequest);

        SaaSFulfillmentSubject subject = saaSFulfillmentRequest.getSubject();
        assertNotNull(subject);
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.CommandEnum.BLOCK, subject.getCommand());
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.TypeEnum.TENANT, subject.getType());

        BlockContext details = (BlockContext) saaSFulfillmentRequest.getDetails();
        assertNotNull(details);
        assertNotNull(details.getTenant());
        assertEquals("atom-spfi-application-uuid", details.getTenant().getSolutionId());
        assertEquals("external-id-1234", details.getTenant().getExternalId());
        assertEquals("cf-eu10", details.getTenant().getSpec().getDataCenter());
        assertEquals("XAF", details.getTenant().getSpec().getSpcDataCenter());
        assertEquals("ZH726", details.getTenant().getSpec().getBusinessType());
        Map<String, Object> properties = details.getAdditionalProperties().getProperties();
        assertNotNull(properties.get("spec.PartnerAppTestTenant"));
    }

    @Test
    public void resolveFulfillmentSsoSetupTest() throws IOException, SpfiClientException {

        stubFor(post(urlPathMatching(TOKEN_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON)
                .withBody(GET_TOKEN_RESPONSE)));

        stubFor(get(urlPathMatching(SpfiClientConstants.RESOLVE_FULFILLMENT_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON)
                .withBody(RESOLVE_FULFILLMENT_SSO_SETUP_RESPONSE)));

        ISpfiOperationsClient spfiOperationsClient = getSpfiOperationsClient();

        SaaSFulfillment saaSFulfillmentRequest = spfiOperationsClient.resolvetFulfillment(TEST_SPFI_NOTIFICATION_ID);

        // check response
        assertNotNull(saaSFulfillmentRequest);

        SaaSFulfillmentSubject subject = saaSFulfillmentRequest.getSubject();
        assertNotNull(subject);
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.CommandEnum.SSOSETUP, subject.getCommand());
        assertEquals(com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.TypeEnum.TENANT, subject.getType());

        SsosetupContext details = (SsosetupContext) saaSFulfillmentRequest.getDetails();
        assertNotNull(details);
        assertNotNull(details.getTenant());
        assertEquals("atom-spfi-application-uuid", details.getTenant().getSolutionId());

        Identityprovider identityProvider = details.getIdentityProvider();
        assertNotNull(identityProvider);
        assertEquals("metadata-url", identityProvider.getMetadataURL());
        assertEquals("saml2", identityProvider.getSsoType());
    }

    @Test
    public void updateFulfillmentTest() throws IOException, SpfiClientException {

        stubFor(post(urlPathMatching(TOKEN_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON)
                .withBody(GET_TOKEN_RESPONSE)));
        
        stubFor(post(urlPathMatching(SpfiClientConstants.UPDATE_STATUS_URL))
            .willReturn(aResponse()
                .withStatus(200)));

        ISpfiOperationsClient spfiOperationsClient = getSpfiOperationsClient();

        Status status = new Status();

        StatusSubject subject = new StatusSubject();
        subject.setCommand(CommandEnum.ACTIVATE);
        subject.setStatus(StatusEnum.COMPLETED);
        subject.setType(TypeEnum.TENANT);
        subject.setId("tenant-id-1234");

        status.setSubject(subject);
        
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
        status.setDetails(activate);

        spfiOperationsClient.updateFulfillment(TEST_SPFI_NOTIFICATION_ID, status);
    }

    private ISpfiOperationsClient getSpfiOperationsClient() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String endpointUrl = String.format(SAAS_OP_SERVICE_ENDPOINT, port);
        ISpfiOperationsClient spfiOperationsClient = SpfiOperationsClientFactory.getInstance()
                                                                                .createSpfiOperationsClient(httpClient, endpointUrl,
                                                                                    new ClientCredentials(endpointUrl,
                                                                                        "client-id", "pass"));
        return spfiOperationsClient;
    }
}
