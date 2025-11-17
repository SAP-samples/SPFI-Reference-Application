package com.sap.lm.sl.spfi.refapp.persistence;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import com.sap.lm.sl.spfi.refapp.controllers.NotFoundException;
import com.sap.lm.sl.spfi.refapp.services.AppTenant;

public class FileTenantRepository implements ITenantRepository{
    public static final String TIMESTAMP_WEB_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; // 2017-12-27T23:45:32.999Z RFC-3339 with millisecond fractions, UTC/Zulu time
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void storeTenant(AppTenant tenant) throws AppException {
        String sapCrmTenantId = tenant.getSapCrmId();

        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_WEB_FORMAT);
        String currentDataTime = dateFormat.format(date);

        // create directory if necessary
        new File(getUploadPath()).mkdirs();
        String tenantId = null;
        try {
            AppTenant existingTenant = readTenantByCrmTenantId(sapCrmTenantId);
            tenantId = existingTenant.getId();
            tenant.setCreatedOn(existingTenant.getCreatedOn());
            tenant.setUpdatedOn(currentDataTime);
            logger.debug("Tenant with CRM-Tenant-ID {} already exists", sapCrmTenantId);
            logger.debug("Updating tenant with ID {}, CRM-Tenant-ID {} on the file system", tenantId, sapCrmTenantId);
        } catch (NotFoundException e) {
            tenantId = UUID.randomUUID().toString();
            tenant.setCreatedOn(currentDataTime);
            logger.debug("Storing tenant with ID {}, CRM-Tenant-ID {} on the file system", tenantId, sapCrmTenantId);
        }
        tenant.setId(tenantId);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String fullFileName = getUploadPath() + MessageFormat.format("{0}_{1}", tenantId, sapCrmTenantId);
        logger.debug("Storage path: " + fullFileName);
        try {
            writer.writeValue(new File(fullFileName), tenant);
        } catch (IOException e) {
            logger.error(MessageFormat.format("Could not store tenant data to file: {0}", fullFileName), e);
            throw new AppException(MessageFormat.format("Could not store tenant data to file: {0}", fullFileName), e);
        }
        logger.info(MessageFormat.format("Tenant successfully stored in the file {0}", fullFileName));
    }

    @Override
    public AppTenant readTenantByCrmTenantId(String sapCrmTenantId) throws NotFoundException, AppException {
        logger.debug("Reading tenant with CRM-Tenant-ID {} from file system", sapCrmTenantId);
        ObjectMapper mapper = new ObjectMapper();
        String fileName = getUploadPath() + findFile(sapCrmTenantId);
        AppTenant tenant = null;
        try {
            tenant = mapper.readValue(new File(fileName), AppTenant.class);
        } catch (IOException e) {
            logger.error(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
            throw new AppException(MessageFormat.format("Could not read tenant data from file: {0}", fileName), e);
        }
        return tenant;
    }

    private String findFile(String sapCrmTenantId) throws NotFoundException, AppException {
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
        throw new NotFoundException(MessageFormat.format("Tenant with CRM-Tenant-ID {0} does not exist", sapCrmTenantId));
    }

    private AppTenant readTenantByFileName(String fileName) throws AppException {
        logger.debug("Reading tenant from file system");
        ObjectMapper mapper = new ObjectMapper();
        String fullFileName = getUploadPath() + fileName;
        AppTenant tenant = null;
        try {
            tenant = mapper.readValue(new File(fullFileName), AppTenant.class);
        } catch (IOException e) {
            logger.error(MessageFormat.format("Could not read tenant data from file: {0}", fullFileName), e);
            throw new AppException(MessageFormat.format("Could not read tenant data from file: {0}", fullFileName), e);
        }
        return tenant;
    }

    @Override
    public void deleteTenantByCrmTenantId(String sapCrmTenantId) throws AppException {
        logger.debug("Deleting file for CRM-Tenant-ID {} from file system", sapCrmTenantId);

        // check whether tenant exists
        if (!tenantExists(sapCrmTenantId)) {
            logger.info("Tenant with CRM-Tenant-ID {} does not exist", sapCrmTenantId);
            //throw new NotFoundException(MessageFormat.format("Tenant with CRM-Tenant-ID {0} does not exist", sapCrmTenantId));
        }

        String fileName = getUploadPath() + findFile(sapCrmTenantId);
        File file = new File(fileName);
        if (file.delete()) {
            logger.info(MessageFormat.format("File {0} deleted successfully", fileName));
        } else {
            logger.error(MessageFormat.format("Failed to delete the file {0}", fileName));
            throw new AppException(MessageFormat.format("Failed to delete the file {0}", fileName));
        }
    }

    private boolean tenantExists(String sapCrmTenantId) throws AppException {
        try {
            readTenantByCrmTenantId(sapCrmTenantId);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public List<String> listTenants() throws AppException {
        // get list of tenants (CRM-Teant-IDs)
        logger.debug("Getting tenant list");
        File folder = new File(getUploadPath());
        logger.debug("File storage path: "+folder.getAbsolutePath());
        File[] listOfFiles = folder.listFiles();
        List<String> tenants = new ArrayList<>();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                File file = listOfFiles[i];
                if (file.isFile()) {
                    tenants.add(getCrmTenantId(file.getName()));
                }
            }
        }
        return tenants;
    }

    public static String getCrmTenantId(String fileName) throws AppException {
        String[] ids = fileName.split("_");
        if (ids.length<2) {
            throw new AppException(MessageFormat.format("Could not parse file name {0}", fileName));
        }
        return ids[1];
    }

    @Override
    public Map<String, AppTenant> readTenants() throws AppException {
        // currently not used
        logger.debug("Getting tenants");
        File folder = new File(getUploadPath());
        File[] listOfFiles = folder.listFiles();
        HashMap<String, AppTenant> tenants = new HashMap<>();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String fileName = listOfFiles[i].getName();
                    AppTenant tenant = readTenantByFileName(fileName);
                    tenants.put(tenant.getId(), tenant);
                }
            }
        }
        return tenants;
    }

    public static String getUploadPath() {
        String uploadPath = String.format("%s%stenants%s", System.getenv("TEMP"), File.separator, File.separator);
        return uploadPath;
    }

}
