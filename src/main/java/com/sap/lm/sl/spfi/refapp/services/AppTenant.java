package com.sap.lm.sl.spfi.refapp.services;

import java.util.List;

import com.sap.lm.sl.spfi.operations.client.model.Customer;
import com.sap.lm.sl.spfi.operations.client.model.Endpoint;
import com.sap.lm.sl.spfi.operations.client.model.SsosetupApplicationProvider;
import com.sap.lm.sl.spfi.operations.client.model.TenantSpec;
import com.sap.lm.sl.spfi.operations.client.model.User;

public class AppTenant {
    private String id;
    private String sapCrmId;
    private String solutionId = null;
    private TenantSpec spec = null;
    private List<Endpoint> endpoints = null;
    private Customer customer = null;
    private List<User> initialUsers = null;
    private SsosetupApplicationProvider ssosetupApplicationProvider = null;
    private String createdOn;
    private String updatedOn;
    private boolean blocked = false;
    private String serviceInstanceId;
    private String subscriptionId;
    private String btpSubaccountId;
    private String sapSpfiNotifcationId;

    public String getId() {
        return id;
    }

    public void setId(String tenantId) {
        this.id = tenantId;
    }

    public String getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }

    public TenantSpec getSpec() {
        return spec;
    }

    public void setSpec(TenantSpec spec) {
        this.spec = spec;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<User> getinitialUsers() {
        return initialUsers;
    }

    public void setinitialUsers(List<User> initialUsers) {
        this.initialUsers = initialUsers;
    }

    public String getSapSpfiNotifcationId() {
        return sapSpfiNotifcationId;
    }

    public void setSapSpfiNotifcationId(String sapSpfiNotifcationId) {
        this.sapSpfiNotifcationId = sapSpfiNotifcationId;
    }

    public String getSapCrmId() {
        return sapCrmId;
    }

    public void setSapCrmId(String sapCrmId) {
        this.sapCrmId = sapCrmId;
    }

    public SsosetupApplicationProvider getSsosetupApplicationProvider() {
        return ssosetupApplicationProvider;
    }

    public void setSsosetupApplicationProvider(SsosetupApplicationProvider ssosetupApplicationProvider) {
        this.ssosetupApplicationProvider = ssosetupApplicationProvider;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getBtpSubaccountId() {
        return btpSubaccountId;
    }

    public void setBtpSubaccountId(String btpSubaccountId) {
        this.btpSubaccountId = btpSubaccountId;
    }

}
