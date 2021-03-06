package com.sap.cloud.lm.sl.cf.web.security;

import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.InvalidSignatureException;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.lm.sl.cf.client.util.TokenUtil;
import com.sap.cloud.lm.sl.cf.core.auditlogging.AuditLoggingProvider;
import com.sap.cloud.lm.sl.cf.core.util.Configuration;
import com.sap.cloud.lm.sl.cf.core.util.SSLUtil;
import com.sap.cloud.lm.sl.cf.core.util.SecurityUtil;
import com.sap.cloud.lm.sl.common.util.Pair;

public class CustomTokenServices implements ResourceServerTokenServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTokenServices.class);

    private static final Charset CHARSET = Charset.forName("US-ASCII");

    @Autowired
    @Qualifier("tokenStore")
    TokenStore tokenStore;

    private Configuration configuration = Configuration.getInstance();

    private RestOperations restTemplate;

    private Pair<String, String> tokenKey;

    public CustomTokenServices() {
        restTemplate = new RestTemplate();
        if (configuration.shouldSkipSslValidation()) {
            SSLUtil.disableSSLValidation();
        }
    }

    @Override
    public OAuth2Authentication loadAuthentication(String tokenString) throws AuthenticationException, InvalidTokenException {

        // Get an access token for the specified token string
        OAuth2AccessToken token = readAccessToken(tokenString);

        // Check if a valid access token has been obtained
        if (token == null) {
            logToAuditLogAndThrow("Invalid access token");
        }

        // Check if the token has expired and there is no refresh token
        if (token.isExpired() && token.getRefreshToken() == null) {
            tokenStore.removeAccessToken(token);
            logToAuditLogAndThrow(MessageFormat.format("The access token has expired on {0}", token.getExpiration()));
        }

        // Check if an authentication for this token already exists in the token store
        OAuth2Authentication auth = tokenStore.readAuthentication(token);
        if (auth == null) {
            // Verify the token signature
            try {
                verifyToken(tokenString);
            } catch (InvalidSignatureException e) {
                logToAuditLogAndThrow("Invalid access token signature", e);
            }

            // Create an authentication for the token and store it in the token store
            auth = SecurityUtil.createAuthentication(TokenUtil.getTokenClientId(token), token.getScope(),
                SecurityUtil.getTokenUserInfo(token));
            try {
                tokenStore.storeAccessToken(token, auth);
            } catch (DataIntegrityViolationException e) {
                LOGGER.debug(com.sap.cloud.lm.sl.cf.core.message.Messages.ERROR_STORING_TOKEN_DUE_TO_INTEGRITY_VIOLATION, e);
                // Ignoring the exception as the token and authentication are already persisted
                // by another client.
            }
        }

        return auth;
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenString) {
        // Check if an access token for the received token string already exists in the token store
        OAuth2AccessToken token = tokenStore.readAccessToken(tokenString);
        if (token == null) {
            // Create a new token from the received token string
            if (configuration.areDummyTokensEnabled() && tokenString.equals(TokenUtil.DUMMY_TOKEN)) {
                token = TokenUtil.createDummyToken("dummy", SecurityUtil.CLIENT_ID);
            } else {
                token = TokenUtil.createToken(tokenString);
            }
        }
        return token;
    }

    private void logToAuditLogAndThrow(String message, Exception e) throws InvalidTokenException {
        AuditLoggingProvider.getFacade().logSecurityIncident(message);
        throw new InvalidTokenException(message, e);
    }

    private void logToAuditLogAndThrow(String message) throws InvalidTokenException {
        AuditLoggingProvider.getFacade().logSecurityIncident(message);
        throw new InvalidTokenException(message);
    }

    /**
     * Verifies the specified token, refreshing the cached token verification key if needed.
     * 
     * @param tokenString
     */
    protected void verifyToken(String tokenString) {
        try {
            verify(tokenString);
        } catch (InvalidSignatureException e) {
            refreshTokenKey();
            verify(tokenString);
        }
    }

    private void verify(String tokenString) {
        if (configuration.areDummyTokensEnabled() && tokenString.equals(TokenUtil.DUMMY_TOKEN)) {
            return;
        }
        JwtHelper.decodeAndVerify(tokenString, getSignatureVerifier(getCachedTokenKey()));
    }

    private static SignatureVerifier getSignatureVerifier(Pair<String, String> tokenKey) {
        String key = tokenKey._1;
        String alg = tokenKey._2;
        SignatureVerifier verifier = null;
        // TODO: Find or implement a factory, which would support other algorithms like SHA384withRSA, SHA512withRSA and HmacSHA512.
        if (alg.equals("SHA256withRSA") || alg.equals("RS256"))
            verifier = new RsaVerifier(key);
        else if (alg.equals("HMACSHA256") || alg.equals("HS256"))
            verifier = new MacSigner(key);
        else
            throw new InternalAuthenticationServiceException("Unsupported verifier algorithm " + alg);
        return verifier;
    }

    private Pair<String, String> getCachedTokenKey() {
        if (tokenKey == null) {
            synchronized (this.getClass()) {
                if (tokenKey == null) {
                    refreshTokenKey();
                }
            }
        }
        return tokenKey;
    }

    private void refreshTokenKey() {
        Pair<String, String> tempTokenKey = readTokenKey(readTokenEndpoint(configuration.getTargetURL()), null);
        tokenKey = tempTokenKey;
    }

    @SuppressWarnings("unchecked")
    private URL readTokenEndpoint(URL targetURL) {
        try {
            String infoURL = targetURL.toString() + "/v2/info";
            Map<String, Object> infoMap = restTemplate.getForEntity(infoURL, Map.class).getBody();
            if (infoMap == null) {
                throw new InternalAuthenticationServiceException("Invalid response returned from /v2/info");
            }
            Object endpoint = infoMap.get("token_endpoint");
            if (endpoint == null)
                endpoint = infoMap.get("authorizationEndpoint");
            if (endpoint == null) {
                throw new InternalAuthenticationServiceException("Response from /v2/info does not contain a valid token endpoint");
            }
            return new URL(endpoint.toString());
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException("Could not read token endpoint", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Pair<String, String> readTokenKey(URL uaaUrl, String auth) {
        try {
            String tokenKeyURL = uaaUrl.toString() + "/token_key";
            Map<String, Object> tokenKeyMap = null;
            if (auth != null && !auth.isEmpty()) {
                tokenKeyMap = restTemplate.exchange(tokenKeyURL, HttpMethod.GET, getEntityWithAuthorization(auth), Map.class).getBody();
            } else {
                tokenKeyMap = restTemplate.getForEntity(tokenKeyURL, Map.class).getBody();
            }
            if (tokenKeyMap == null) {
                throw new InternalAuthenticationServiceException("Invalid response returned from /token_key");
            }
            Object value = tokenKeyMap.get("value");
            Object alg = tokenKeyMap.get("alg");
            if (value == null || alg == null) {
                throw new InternalAuthenticationServiceException("Response from /token_key does not contain a key value or an algorithm");
            }
            return new Pair<>(value.toString(), alg.toString());
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException("Could not read token verification key", e);
        }
    }

    private static HttpEntity<?> getEntityWithAuthorization(String auth) {
        String encoded = Base64.getUrlEncoder().encodeToString(auth.getBytes(CHARSET));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Basic " + encoded);
        return new HttpEntity<Object>(requestHeaders);
    }
}
