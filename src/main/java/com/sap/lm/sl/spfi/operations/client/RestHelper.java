package com.sap.lm.sl.spfi.operations.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.lm.sl.spfi.operations.client.model.ServerError;
import com.sap.lm.sl.spfi.operations.client.model.UnauthorizedError;


public class RestHelper {

    private static final int RESPONSE_BODY_LOG_MAX_LENGTS = 400;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper objectMapper;
    private static final String CONTENT_TYPE = "application/json";
    private HttpClient httpClient;

    public RestHelper(HttpClient httpClient) {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        this.httpClient = httpClient;
    }

    private HttpClient getHttpClient() {
        return httpClient;
    }

    public String handleResponse(HttpResponse response, String operation, String httpPath) throws SpfiClientException {
        int status = response.getStatusLine().getStatusCode();
        logger.debug(MessageFormat.format("Status code for operation [{0}]: {1}", operation, status));
        String responseBody = getResponseBody(response, operation);
        logger.debug(MessageFormat.format("Respone for operation [{0}]: {1}", operation, responseBody));
        switch (status) {
            case HttpStatus.SC_CREATED:
                return responseBody;
            case HttpStatus.SC_OK:
                return responseBody;
            case HttpStatus.SC_UNAUTHORIZED:
                logger.error(MessageFormat.format("Received unauthorized error response to operation ''{0}'', body: {1}", operation, responseBody));
                throw new SpfiClientException(
                    MessageFormat.format(SpfiClientMessages.UNAUTHORIZED_ERROR, operation, parseUnauthorizedError(responseBody).getError_description()));
            case HttpStatus.SC_NOT_FOUND:
                logger.error(MessageFormat.format("Received Not Found error response to operation ''{0}'', body: {1}", operation, responseBody));
                throw new SpfiClientUrlNotFoundException(
                    MessageFormat.format(SpfiClientMessages.NOT_FOUND_ERROR, operation));
            default:
                // try to parse
                ServerError serverError = null;
                try {
                    serverError = objectMapper.readValue(responseBody, ServerError.class);
                } catch (IOException e) {
                    logger.debug(MessageFormat.format("Unexpected response to operation [{0}], status code: {1}, body: {2}", operation, status, responseBody), e);
                }
                if (serverError != null && serverError.getError() != null) {
                    logger.error(MessageFormat.format("Operation ''{0}'' to the URL ''{1}'' failed. Remote server responded with HTTP status code {2}, error message: ''{3}'', error code ''{4}''", 
                        operation, httpPath, status, serverError.getError().getMessage(), serverError.getError().getCode()));
                    throw new SpfiClientException(MessageFormat.format(SpfiClientMessages.APP_ERROR, 
                        operation, httpPath, status, serverError.getError().getMessage(), serverError.getError().getCode()));
                } else {
                    logger.error(MessageFormat.format("Operation ''{0}'' to the URL ''{1}'' failed. Remote server responded with HTTP status code {2}, response body: ''{3}''", 
                        operation, httpPath, status, abbreviate(responseBody, 400)));
                    throw new SpfiClientException(MessageFormat.format(SpfiClientMessages.APP_ERROR_BODY, 
                        operation, httpPath, status, abbreviate(responseBody, RESPONSE_BODY_LOG_MAX_LENGTS)));
                }
        }
    }

    private static String abbreviate(String input, int length) {
        if (input==null) {
            return "";
        }
        if (input.length() > length) {
            return input.substring(0, length)+"...";
        } else {
            return input;
        }
    }

    private String getResponseBody(HttpResponse response, String operation) throws SpfiClientException {
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity == null) {
            logger.error("No HTTP entity received");
            throw new SpfiClientException(MessageFormat.format(SpfiClientMessages.UNEXPECTED_RESPONSE_ERROR, operation));
        }
        String entity = null;
        try {
            entity = EntityUtils.toString(httpEntity);
        } catch (ParseException | IOException e) {
            handleReportableException(MessageFormat.format(SpfiClientMessages.UNEXPECTED_RESPONSE_ERROR, operation), e);
        }
        return entity;
    }

    private UnauthorizedError parseUnauthorizedError(String errorJson) throws SpfiClientException {
        UnauthorizedError unauthorizedError = null;
        try {
            unauthorizedError = objectMapper.readValue(errorJson, UnauthorizedError.class);
        } catch (IOException e) {
            logger.error(MessageFormat.format(SpfiClientMessages.SERVER_RESPONSE, errorJson));
            logger.debug("Failed to parse server response", e);
            return new UnauthorizedError(errorJson, UnauthorizedError.class.toString());
        }
        return unauthorizedError;
    }

    public String doHttpGet(String httpPath, Header[] headers, String operation) throws SpfiClientException {
        HttpClient localHttpClient = getHttpClient();
        HttpGet httpget = new HttpGet(httpPath);
        httpget.setHeaders(headers);
        try {
            HttpResponse response = localHttpClient.execute(httpget);
            return handleResponse(response, operation, httpPath);
        } catch (IOException e) {
            handleReportableException(MessageFormat.format(SpfiClientMessages.COMMUNICATION_ERROR, e.getMessage()), e);
            return null;
        }
    }

    public String doHttpPost(String httpPath, Object requestBody, Header[] headers, String operation) throws SpfiClientException {
        HttpClient localHttpClient = getHttpClient();
        String updateStatusReq = null;
        try {
            updateStatusReq = objectMapper.writeValueAsString(requestBody);
            logger.debug("Sending {} to {} with body {}", operation, httpPath, updateStatusReq);
        } catch (IOException e) {
            handleReportableException(MessageFormat.format(SpfiClientMessages.SERIALIZATION_ERROR, operation), e);
            return null;
        }
        HttpPost httpPost = new HttpPost(httpPath);
        try {
            httpPost.setEntity(new StringEntity(updateStatusReq));
        } catch (UnsupportedEncodingException e1) {
            handleNonReportableException(SpfiClientMessages.ENCODING_ERROR, e1);
        }
        httpPost.setHeaders(headers);
        httpPost.addHeader("Accept", CONTENT_TYPE);
        httpPost.addHeader("Content-type", CONTENT_TYPE);

        try {
            HttpResponse response = localHttpClient.execute(httpPost);
            return handleResponse(response, operation, httpPath);
        } catch (IOException e) {
            handleReportableException(MessageFormat.format(SpfiClientMessages.COMMUNICATION_ERROR, e.getMessage()), e);
            return null;
        }
    }

    public String doHttpPatch(String httpPath, Object requestBody, String operation) throws SpfiClientException {
        HttpClient localHttpClient = getHttpClient();
        String createDeployResourceReq = null;
        try {
            createDeployResourceReq = objectMapper.writeValueAsString(requestBody);
        } catch (IOException e) {
            handleReportableException(MessageFormat.format(SpfiClientMessages.SERIALIZATION_ERROR, operation), e);
            return null;
        }
        HttpPatch httpPatch = new HttpPatch(httpPath);
        try {
            httpPatch.setEntity(new StringEntity(createDeployResourceReq));
        } catch (UnsupportedEncodingException e1) {
            handleNonReportableException(SpfiClientMessages.ENCODING_ERROR, e1);
        }
        httpPatch.setHeader("Accept", CONTENT_TYPE);
        httpPatch.setHeader("Content-type", CONTENT_TYPE);
        try {
            HttpResponse response = localHttpClient.execute(httpPatch);
            return handleResponse(response, operation, httpPath);
        } catch (IOException e) {
            handleReportableException(MessageFormat.format(SpfiClientMessages.COMMUNICATION_ERROR, e.getMessage()), e);
            return null;
        }
    }

    public String doHttpDelete(String httpPath, String operation) throws SpfiClientException {
        HttpClient localHttpClient = getHttpClient();
        try {
            HttpDelete httpget = new HttpDelete(httpPath);
            HttpResponse response = localHttpClient.execute(httpget);
            return handleResponse(response, operation, httpPath);
        } catch (IOException e) {
            handleReportableException(MessageFormat.format(SpfiClientMessages.COMMUNICATION_ERROR, e.getMessage()), e);
            return null;
        }
    }

    public void closeQuietly(final HttpResponse response) {
        if (response != null) {
            try {
                EntityUtils.consume(response.getEntity());
            } catch (final IOException ignore) {
                logger.debug("Could not close HTTP Response", ignore);
            }
        }
    }

    public void handleReportableException(String message, Throwable e) throws SpfiClientException {
        String msg = message + ": " + e.getMessage();
        logger.error(msg, e);
        throw new SpfiClientException(msg, e);
    }

    public void handleNonReportableException(String detailedExternalMessage, Throwable e) throws SpfiClientException {
        String msg = SpfiClientMessages.GENERIC_ERROR + ": " + detailedExternalMessage;
        logger.error(msg, e);
        throw new SpfiClientException(msg);
    }

    public void handleArgumentValidationException(String message) throws SpfiClientException {
        String msg = SpfiClientMessages.GENERIC_ERROR;
        logger.error(msg + ": " + message);
        throw new SpfiClientException(msg);
    }

    public void traceUrl(String operation, String url) {
        logger.debug(MessageFormat.format("Sending request to url {0} for operation [{1}]", url, operation));
    }

    public void validateUrl(String operation, String url) throws SpfiClientException {
        String[] schemes = {"http","https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (!urlValidator.isValid(url)) {
            String message = MessageFormat.format("Invalid URL ''{0}'' configured for operation {1}", url, operation);
            logger.error(message);
            throw new SpfiClientException(message);
        }
    }

}
