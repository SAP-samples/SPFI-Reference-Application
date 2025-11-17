package com.sap.lm.sl.spfi.refapp.mocks;

public class MockControllerConstants {
    public static final String SAAS_OPERATOR_BASE_PATH = "/saasfulfillment/v1";
    public static final String SAAS_OPERATOR_RESOLVE_REL_PATH = "/resolve";
    public static final String SAAS_OPERATOR_RESOLVE_ABS_PATH = SAAS_OPERATOR_BASE_PATH + SAAS_OPERATOR_RESOLVE_REL_PATH;
    public static final String SAAS_OPERATOR_UPDATE_STATUS_REL_PATH = "/command/status";
    public static final String SAAS_OPERATOR_UPDATE_STATUS_ABS_PATH = SAAS_OPERATOR_BASE_PATH + SAAS_OPERATOR_UPDATE_STATUS_REL_PATH;

    public static final String TENANTS_PATH = "/tenants";
    public static final String TENANT_PATH = "/tenants/{tenantId}";
    public static final String IASTEST_PATH = "/iastest";
}
