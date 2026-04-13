package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.AppException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.NotFoundException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.StateRequest;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Status;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State.ACTIVE;

@Repository
public class FileTenantRepository implements ITenantRepository {
    public static final String TIMESTAMP_WEB_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; // 2017-12-27T23:45:32.999Z RFC-3339 with millisecond fractions, UTC/Zulu time
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public Tenant storeTenant(Tenant tenant) throws AppException {
        // create directory if necessary
        new File(getUploadPath()).mkdirs();
        createOrUpdateFile(tenant);
        return tenant;
    }

    @Override
    public void updateTenant(String tenantId, Tenant tenantData) throws AppException {
        logger.debug("Starting tenant update, Tenant-ID: {}", tenantId);
        Tenant tenant = readTenantByTenantId(tenantId);
        if (tenantData.getCustomer() != null) {
            tenant.setCustomer(tenantData.getCustomer());
        }
        if (!tenantData.getInitialUsers().isEmpty()) {
            tenant.setInitialUsers(tenantData.getInitialUsers());
        }
        if (tenantData.getAdditionalProperties() != null ) {
            tenant.setAdditionalProperties(tenantData.getAdditionalProperties());
        }
        TenantUtils tenantUtils = new TenantUtils();
        tenant = tenantUtils.setTenantState(tenant, ACTIVE.getReadableState());
        createOrUpdateFile(tenant);
    }

    @Override
    public Tenant readTenantByCrmTenantId(String sapCrmTenantId) throws AppException {
        logger.debug("Reading tenant with CRM-Tenant-ID {} from file system", sapCrmTenantId);
        ObjectMapper mapper = new ObjectMapper();
        String fileName = findFile(sapCrmTenantId);
        if (fileName == null) {
            return null;
        }
        String filePath = getUploadPath() + fileName;
        Tenant tenant;
        try {
            tenant = mapper.readValue(new File(filePath), Tenant.class);
        } catch (IOException e) {
            logger.error(MessageFormat.format("Could not read tenant data from file: {0}", filePath), e);
            throw new AppException(MessageFormat.format("Could not read tenant data from file: {0}", filePath), e);
        }
        return tenant;
    }


    @Override
    public Tenant readTenantByTenantId(String tenantId) throws NotFoundException, AppException {
        logger.debug("Reading tenant with Tenant-ID {} from file system", tenantId);
        ObjectMapper mapper = new ObjectMapper();
        String fileName = getUploadPath() + findFileByTenantId(tenantId);
        Tenant tenant = null;
        try {
            tenant = mapper.readValue(new File(fileName), Tenant.class);
        } catch (IOException e) {
            logger.error(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
            throw new AppException(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
        }
        return tenant;
    }

    @Override
    public List<Tenant> readTenants() throws AppException {
        // get list of tenants (CRM-Tenant-IDs)
        logger.debug("Getting tenant list");
        File folder = new File(getUploadPath());
        logger.debug("File storage path: "+folder.getAbsolutePath());
        File[] listOfFiles = folder.listFiles();
        List<Tenant> tenants = new ArrayList<>();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                File file = listOfFiles[i];
                if (file.isFile()) {
                    try {
                        tenants.add(readTenantByTenantId(getTenantId(file.getName())));
                    } catch (AppException | NotFoundException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return tenants;
    }

    private String findFile(String sapCrmTenantId) throws AppException {
        logger.debug("Searching for file for CRM-Tenant-ID {}", sapCrmTenantId);
        File folder = new File(getUploadPath());
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                File file = listOfFiles[i];
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (sapCrmTenantId.equals(getCrmTenantId(fileName))) {
                        return fileName;
                    }
                }
            }
        }
        logger.debug("Tenant with CRM-Tenant-ID {} does not exist", sapCrmTenantId);
        return null;
    }

    private String findFileByTenantId(String tenantId) throws NotFoundException, AppException {
        logger.debug("Searching for file for Tenant-ID {}", tenantId);
        File folder = new File(getUploadPath());
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                File file = listOfFiles[i];
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (tenantId.equals(getTenantId(fileName))) {
                        return fileName;
                    }
                }
            }
        }
        throw new NotFoundException(MessageFormat.format("Tenant with Tenant-ID {0} does not exist", tenantId));
    }

    @Override
    public void deleteTenantByTenantId(String tenantId) throws AppException {
        logger.debug("Deleting file for Tenant-ID {} from file system", tenantId);
        String fileName = getUploadPath() + findFileByTenantId(tenantId);
        File file = new File(fileName);
        if (file.delete()) {
            logger.info(MessageFormat.format("File {0} deleted successfully", fileName));
        } else {
            logger.error(MessageFormat.format("Failed to delete the file {0}", fileName));
            throw new AppException(MessageFormat.format("Failed to delete the file {0}", fileName));
        }
    }

    @Override
    public Status getTenantStatus(String tenantId) throws AppException {
        logger.debug("Reading tenant with Tenant-ID {} from file system", tenantId);
        ObjectMapper mapper = new ObjectMapper();
        String fileName = getUploadPath() + findFileByTenantId(tenantId);
        Tenant tenant = null;
        try {
            tenant = mapper.readValue(new File(fileName), Tenant.class);
        } catch (IOException e) {
            logger.error(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
            throw new AppException(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
        }
        return tenant.getStatus();
    }

    @Override
    public Status changeTenantStatus(String tenantId, StateRequest stateRequest) throws AppException {
        logger.debug("Reading tenant with Tenant-ID {} from file system", tenantId);
        ObjectMapper mapper = new ObjectMapper();
        String fileName = getUploadPath() + findFileByTenantId(tenantId);
        Tenant tenant;

        try {
            tenant = mapper.readValue(new File(fileName), Tenant.class);
        } catch (IOException e) {
            logger.error(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
            throw new AppException(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
        }

        String newStatus = stateRequest.getState();
        TenantUtils tenantUtils = new TenantUtils();
        Status status = tenantUtils.getStatus(newStatus);
        tenant.setStatus(status);
        createOrUpdateFile(tenant);

        return tenant.getStatus();
    }

    public void createOrUpdateFile(Tenant tenant) throws AppException {
        String fullFileName = getUploadPath() + MessageFormat.format("{0}_{1}", tenant.getId(), tenant.getSapId());
        logger.debug("Storage path: " + fullFileName);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(new File(fullFileName), tenant);
        } catch (IOException ex) {
            logger.error(MessageFormat.format("Could not store tenant data to file: {0}", fullFileName), ex);
            throw new AppException(MessageFormat.format("Could not store tenant data to file: {0}", fullFileName), ex);
        }
        logger.info(MessageFormat.format("Tenant successfully stored in the file {0}", fullFileName));
    }

    public boolean isTenantExist(String tenantId) throws AppException {       // checks if tenant is already present with tenant-id or not
        try {
            readTenantByTenantId(tenantId);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public static String getTenantId(String fileName) throws AppException {
        String[] ids = fileName.split("_");
        if (ids.length<2) {
            throw new AppException(MessageFormat.format("Could not parse file name {0}", fileName));
        }
        return ids[0];
    }


    public static String getUploadPath() {
        String uploadPath = String.format("%s%stenants%s", System.getenv("TEMP"), File.separator, File.separator);
        return uploadPath;
    }

    public static String getCrmTenantId(String fileName) throws AppException {
        String[] ids = fileName.split("_");
        if (ids.length<2) {
            throw new AppException(MessageFormat.format("Could not parse file name {0}", fileName));
        }
        return ids[1];
    }

}
