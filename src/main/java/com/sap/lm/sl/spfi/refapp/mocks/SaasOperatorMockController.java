package com.sap.lm.sl.spfi.refapp.mocks;


import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sap.lm.sl.spfi.operations.client.model.ActivateContext;
import com.sap.lm.sl.spfi.operations.client.model.BlockContext;
import com.sap.lm.sl.spfi.operations.client.model.Customer;
import com.sap.lm.sl.spfi.operations.client.model.TerminateContext;
import com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillment;
import com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject;
import com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.CommandEnum;
import com.sap.lm.sl.spfi.operations.client.model.SaaSFulfillmentSubject.TypeEnum;
import com.sap.lm.sl.spfi.operations.client.model.SsosetupContext;
import com.sap.lm.sl.spfi.refapp.controllers.BadRequestException;
import com.sap.lm.sl.spfi.refapp.controllers.ControllersConstants;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(path = MockControllerConstants.SAAS_OPERATOR_BASE_PATH)
public class SaasOperatorMockController {

    private static final Logger logger = LoggerFactory.getLogger(SaasOperatorMockController.class);
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    private static final String CRM_TENANT_ID = "test-crm-tenant-id";
    private static final String ACTIVATE_DETAILS_RESPONSE = "{"
        + "    \"tenant\": {"
        + "      \"solutionId\": \"atom-spfi-application-uuid\","
        + "      \"operationType\": \"9505372\","
        + "      \"id\": \"crm-tenant-id\","
        + "      \"spec\": {"
        + "        \"region\": \"eu\","
        + "        \"dataCenter\": \"cf-eu10\","
        + "        \"spcDataCenter\": \"XAF\","
        + "        \"businessType\": \"ZH726\","
        + "        \"iasUrl\": \"https://demo.accounts400.ondemand.com\","
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
        + "      \"name\": \"Demo customer\","
        + "      \"contact\": {"
        + "        \"name\": \"contact-user-01\","
        + "        \"email\": \"cuser01@demo.com\","
        + "        \"phone\": \"+49 494949498589\""
        + "      }"
        + "    },"
        + "    \"initialUsers\": ["
        + "      {"
        + "        \"name\": \"testuser01\","
        + "        \"email\": \"testuser@demo.com\""
        + "      },"
        + "      {"
        + "        \"name\": \"contact-user-01\","
        + "        \"email\": \"cuser01@demo.com\""
        + "      }"
        + "    ]"
        + "  }";

    private static final String TERMINATE_DETAILS_RESPONSE = "{" +
    "    \"tenant\": {"
    + "      \"solutionId\": \"atom-spfi-application-uuid\","
    + "      \"id\": \"crm-tenant-id\","
    + "      \"externalId\": \"partner-assigned-id\","
    + "      \"spec\": {"
    + "        \"dataCenter\": \"cf-eu10\","
    + "        \"spcDataCenter\": \"XAF\","
    + "        \"businessType\": \"ZH726\""
    + "      }"
    + "    }"
    + "}";

    private static final String SSO_SETUP_DETAILS_RESPONSE = "{"
        + "    \"tenant\": {"
        + "      \"solutionId\": \"atom-spfi-application-uuid\""
        + "    },"
        + "    \"identityProvider\": {"
        + "      \"ssoType\": \"saml2\","
        + "      \"metadataURL\": \"metadata-url\""
        + "    }"
        + "  }";

    private static final String BLOCK_DETAILS_RESPONSE = "{"
        + "    \"tenant\": {"
        + "      \"solutionId\": \"atom-spfi-application-uuid\","
        + "      \"externalId\": \"crm-tenant-id\","
        + "      \"spec\": {"
        + "        \"dataCenter\": \"cf-eu10\","
        + "        \"spcDataCenter\": \"XAF\","
        + "        \"businessType\": \"ZH726\""
        + "      }"
        + "    }"
        + "}";
    
    @org.springframework.beans.factory.annotation.Autowired
    public SaasOperatorMockController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        this.request = request;
    }

    @GetMapping(path = MockControllerConstants.SAAS_OPERATOR_RESOLVE_REL_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SaaSFulfillment> resolve(
      @Parameter(in = ParameterIn.HEADER, description = "* Notification id as received in the notify API. ", schema=@Schema()) @RequestHeader(value=ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, required=true) String xSapSaasfulfillmentNotificationId) {

        logger.debug("Resolving fulfillment, SaasFulfillmentNotificationId: {}", xSapSaasfulfillmentNotificationId);

        SaaSFulfillment saaSFulfillment = null;
        try {
            if (xSapSaasfulfillmentNotificationId.toString().startsWith("a")) {
                saaSFulfillment = resolveActivation(xSapSaasfulfillmentNotificationId);
                if (xSapSaasfulfillmentNotificationId.toString().endsWith(IntegrationTests.MOCK_RESOLVE_FAIL_NOTIFICATION_ID_SUFFIX)) {
                    throw new BadRequestException("Test resolve failure", "010");
                }
            } else if (xSapSaasfulfillmentNotificationId.toString().startsWith("t")){
                saaSFulfillment = resolveTermination(xSapSaasfulfillmentNotificationId);
            } else if (xSapSaasfulfillmentNotificationId.toString().startsWith("s")){
                saaSFulfillment = resolveSsoSetup(xSapSaasfulfillmentNotificationId);
            } else if (xSapSaasfulfillmentNotificationId.toString().startsWith("b")){
                saaSFulfillment = resolveBlocking(xSapSaasfulfillmentNotificationId);
            } else {
                saaSFulfillment = resolveChange(xSapSaasfulfillmentNotificationId);
            }

        } catch (IOException e) {
            logger.error("Couldn't serialize response for resolve fulfillment", e);
            return new ResponseEntity<SaaSFulfillment>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String responseForLog = new Gson().toJson(saaSFulfillment);
        logger.debug("Response from resolve call: {}", responseForLog);

        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<SaaSFulfillment> responseEntity = new ResponseEntity<>(saaSFulfillment, headers, HttpStatus.OK);
        return responseEntity;
    }

    private SaaSFulfillment resolveActivation(String xSapSaasfulfillmentNotificationId) throws IOException {
        SaaSFulfillment saaSFulfillment = new SaaSFulfillment();
        SaaSFulfillmentSubject subject = new SaaSFulfillmentSubject();
        subject.setCommand(CommandEnum.ACTIVATE);
        subject.setType(TypeEnum.TENANT);
        String crmTenantId = CRM_TENANT_ID;
        subject.setId(crmTenantId);

        if (xSapSaasfulfillmentNotificationId.endsWith(IntegrationTests.ACTIVATION_FAIL_TENANT_ID_SUFFIX)) {
            subject.setId(crmTenantId + IntegrationTests.ACTIVATION_FAIL_TENANT_ID_SUFFIX);
        }

        saaSFulfillment.setSubject(subject);
        ActivateContext activateContext = objectMapper.readValue(ACTIVATE_DETAILS_RESPONSE, ActivateContext.class);
        saaSFulfillment = saaSFulfillment.details(activateContext);
        return saaSFulfillment;
    }

    private SaaSFulfillment resolveChange(String xSapSaasfulfillmentNotificationId) throws IOException {
        SaaSFulfillment fulfillment = resolveActivation(xSapSaasfulfillmentNotificationId);
        ActivateContext activateContext = (ActivateContext) fulfillment.getDetails();
        Customer customer = activateContext.getCustomer();
        customer.setName("Updated Customer Name");
        activateContext.setCustomer(customer);
        fulfillment = fulfillment.details(activateContext);
        return fulfillment;
    }
 
    private SaaSFulfillment resolveTermination(String xSapSaasfulfillmentNotificationId) throws IOException {
        SaaSFulfillment saaSFulfillment = new SaaSFulfillment();
        SaaSFulfillmentSubject subject = new SaaSFulfillmentSubject();
        subject.setCommand(CommandEnum.TERMINATE);
        subject.setType(TypeEnum.TENANT);
        String crmTenantId = CRM_TENANT_ID;
        subject.setId(crmTenantId);

        if (xSapSaasfulfillmentNotificationId.endsWith(IntegrationTests.TERMINATION_FAIL_TENANT_ID_SUFFIX)) {
            subject.setId(crmTenantId + IntegrationTests.TERMINATION_FAIL_TENANT_ID_SUFFIX);
        }

        saaSFulfillment.setSubject(subject);
        TerminateContext terminateContext = objectMapper.readValue(TERMINATE_DETAILS_RESPONSE, TerminateContext.class);
        saaSFulfillment = saaSFulfillment.details(terminateContext);
        return saaSFulfillment;
    }

    private SaaSFulfillment resolveSsoSetup(String xSapSaasfulfillmentNotificationId) throws IOException {
        SaaSFulfillment saaSFulfillment = new SaaSFulfillment();
        SaaSFulfillmentSubject subject = new SaaSFulfillmentSubject();
        subject.setCommand(CommandEnum.SSOSETUP);
        subject.setType(TypeEnum.TENANT);
        subject.setId(CRM_TENANT_ID);
        saaSFulfillment.setSubject(subject);
        SsosetupContext ssoSetupContext = objectMapper.readValue(SSO_SETUP_DETAILS_RESPONSE, SsosetupContext.class);
        saaSFulfillment = saaSFulfillment.details(ssoSetupContext);
        return saaSFulfillment;
    }

    private SaaSFulfillment resolveBlocking(String xSapSaasfulfillmentNotificationId) throws IOException {
        SaaSFulfillment saaSFulfillment = new SaaSFulfillment();
        SaaSFulfillmentSubject subject = new SaaSFulfillmentSubject();
        subject.setCommand(CommandEnum.BLOCK);
        subject.setType(TypeEnum.TENANT);
        String crmTenantId = CRM_TENANT_ID;
        subject.setId(crmTenantId);
        saaSFulfillment.setSubject(subject);
        BlockContext blockContext = objectMapper.readValue(BLOCK_DETAILS_RESPONSE, BlockContext.class);
        saaSFulfillment = saaSFulfillment.details(blockContext);
        return saaSFulfillment;
    }

    @PostMapping(path = MockControllerConstants.SAAS_OPERATOR_UPDATE_STATUS_REL_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateStatus(
      @Parameter(in = ParameterIn.HEADER, description = "" , required=true, schema=@Schema()) @RequestHeader(value=ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, required=true) String xSapSaasfulfillmentNotificationId,
      @Parameter(in = ParameterIn.DEFAULT, description = "* status structure should be passed as a request payload with the command specific status sub structure.", schema=@Schema()) @Valid @RequestBody ActivateStatus body,
      BindingResult bindingResult) {

        logger.debug("Processing update fulfillment status request for SaasFulfillmentNotificationId " + xSapSaasfulfillmentNotificationId);

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            logger.error(error.getDefaultMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<Void>(headers, HttpStatus.OK);
    }

}
