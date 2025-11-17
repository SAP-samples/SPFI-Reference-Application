package com.sap.lm.sl.spfi.operations.client;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;


public class TokenProvider {
	public static final String CLIENT_CREDENTIALS_GRAND_TYPE = "client_credentials";
    public static final String PASSWORD_GRAND_TYPE = "password";
    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private String iasUrl;
    private String clientId;
    private String clientSecret;
    private String user;
    private String password;
    private String grantType;
    private boolean isUserToken;
    private String token = null;
    private long tokenExpiresAt = -1;

    public static final String ERROR_GET_OAUTH_TOKEN = "Failed to get a JWT token from IAS";
    public static final String ERROR_HTTP_URL = "The provided URL is invalid. Only HTTP(S) urls are allowed.";
    public static final String ERROR_GET_OAUTH_TOKEN_RESP_CODE = "Request for get an OAuth token from URL ''{0}'' failed with response code {1}";

    public TokenProvider(ClientCredentials clientCredentials){
            //logger.debug("Client Credentials Flow");
            initializeClassFields(clientCredentials.getIasUrl(), clientCredentials.getClientId(), clientCredentials.getClientSecret(), null, null, CLIENT_CREDENTIALS_GRAND_TYPE,
                false);
    }

    private void initializeClassFields(String iasUrl, String clientId, String clientSecret, String user, String password, String grantType, boolean isUserToken) {
        this.iasUrl = iasUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.user = user;
        this.password = password;
        this.grantType = grantType;
        this.isUserToken = isUserToken;
    }

    public String retrieveToken() throws SpfiClientException{
        if(!isValidToken()){
            CloseableHttpClient httpClient = null;
            try {
                httpClient = HttpClients.createDefault();
                String fullIasUrl = iasUrl + "/oauth2/token";
                HttpPost httpPost = new HttpPost(fullIasUrl);
                logger.debug("Building authorization token: client ID: {},  client Secret: {}, iasUrl: {}", clientId, StringUtils.abbreviate(clientSecret,6), iasUrl);
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(clientId, clientSecret);
                try {
                    httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
                } catch (AuthenticationException e) {
                    logger.error(e.getMessage());
                    throw new SpfiClientException(ERROR_GET_OAUTH_TOKEN);
                }

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("grant_type", grantType));
                if (isUserToken) {
                    params.add(new BasicNameValuePair("username", user));
                    params.add(new BasicNameValuePair("password", password));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode == HttpStatus.SC_OK){
                    HttpEntity entity = null;
                    try {
                        entity = response.getEntity();
                        String responseBody = EntityUtils.toString(entity);
                        JSONObject obj = new JSONObject(responseBody);
                        token = obj.getString("access_token");
                        tokenExpiresAt = System.currentTimeMillis() + (1000 * obj.getLong("expires_in")); // in ms.
                    } finally {
                        EntityUtils.consume(entity);
                    }
                    return token;
                } else{
                    logger.error("Failed to get a JWT token from UAA HttpStatus: {}", statusCode);
                    throw new SpfiClientException(MessageFormat.format(ERROR_GET_OAUTH_TOKEN_RESP_CODE, iasUrl, statusCode));
                }
            } catch (JSONException e) {
                logger.error("Failed to parse json by getting a JWT token from UAA", e);
                throw new SpfiClientException(ERROR_GET_OAUTH_TOKEN);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new SpfiClientException(ERROR_GET_OAUTH_TOKEN);
            } catch (RuntimeException e) {
                // special handling since it may include sensitive data,
                // safety fallback for an unexpected exception
                StackTraceElement[] stackTraces = e.getStackTrace();
                String details = "";
                for (StackTraceElement stackTrace : stackTraces) {
                    details = details + stackTrace.toString();
                    if (details.length() > 1000) {
                        break;
                    }
                }
                logger.error("Failed to get a JWT token from UAA", e);
                throw new SpfiClientException(ERROR_GET_OAUTH_TOKEN);
            }
            finally {
                closeQuietly(httpClient);
            }
        } else{
            return token;
        }
    }

    private boolean isValidToken(){
        if(token != null){
            long start = System.currentTimeMillis();
            try {
                long currentTime = new Date().getTime();
                if((currentTime + (60*1000)) < tokenExpiresAt){ // to be sure that the token at least 60 s. is still valid
                    return true;
                } else{
                    return false;
                }
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
                return false;
            } finally{
                long end = System.currentTimeMillis();
                logger.info("Token Validation Time: {} ms.", (end-start));
            }
        } else{
            return false;
        }
    }

    protected void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                logger.warn(ioe.getMessage(), ioe);
            }
        }
    }

    protected String getUaaUrl() {
        return iasUrl;
    }

    protected String getClientId() {
        return clientId;
    }

    protected String getClientSecret() {
        return clientSecret;
    }
    
    public long getTokenExpTime() {
    	return tokenExpiresAt;
    }

}
