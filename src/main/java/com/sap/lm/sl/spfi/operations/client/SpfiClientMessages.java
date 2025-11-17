package com.sap.lm.sl.spfi.operations.client;

public final class SpfiClientMessages { // NOSONAR
    public static final String COMMUNICATION_ERROR = "Error occured during communication with HTTP server: {0}";
    public static final String UNEXPECTED_RESPONSE_ERROR = "Unexpected response from the operation [{0}]";
    public static final String UNAUTHORIZED_ERROR = "Authorization error occurred during processing of operation ''{0}'': {1}";
    public static final String NOT_FOUND_ERROR = "Not Found error occurred during processing of operation ''{0}''";
    public static final String APP_ERROR = "Operation ''{0}'' to the URL ''{1}'' failed. Remote server responded with HTTP status code {2}, error message: ''{3}'', error type ''{4}''";
    public static final String APP_ERROR_BODY = "Operation ''{0}'' to the URL ''{1}'' failed. Remote server responded with HTTP status code {2}, response body: ''{3}''";
    public static final String DESERIALIZATION_ERROR = "Response from the operation ''{0}'' could not be parsed";
    public static final String SERIALIZATION_ERROR = "JSON object could not be generated for {0}";
    public static final String ENCODING_ERROR = "Not supported character encoding";
    public static final String GENERIC_ERROR = "An internal error occured";
    public static final String VERSION_HANDSHAKE_ERROR = "Communication not possible. Minimal supported server version: {0}, highest supported client version: {1}";
    public static final String SERVER_RESPONSE = "Retrieved response: {0}";
    public static final String VALIDATION_FIELD_NULL = "Value can not be null";
    public static final String OPERATION_VALIDATION_ERROR = "Validation for {0} of the operation ''{1}'' failed with an error in field ''{2}'': ''{3}''; rejected value: ''{4}''";
    public static final String VALIDATION_TYPE_REQUEST = "request";
    public static final String VALIDATION_TYPE_RESPONSE = "response";
    public static final String VERSION_HANDSHAKE_OLD_SERVICE_KEY = "An old service key with content_endpoint {0} is used. Please recreate your service key";
}
