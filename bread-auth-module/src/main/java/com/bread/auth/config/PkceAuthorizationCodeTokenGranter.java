package com.bread.auth.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.HashMap;
import java.util.Map;

public class PkceAuthorizationCodeTokenGranter extends AuthorizationCodeTokenGranter {

    private final PkceAuthorizationCodeService pkceAuthorizationCodeService;

    public PkceAuthorizationCodeTokenGranter(AuthorizationServerTokenServices tokenServices, PkceAuthorizationCodeService pkceAuthorizationCodeService,
                                             ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        super(tokenServices, pkceAuthorizationCodeService, clientDetailsService, requestFactory);
        this.pkceAuthorizationCodeService = pkceAuthorizationCodeService;
    }

    /**
     * code 발급 후, 토큰 교환 엔드포인트 접근 시
     *
     * @param client       클라이언트 정보
     * @param tokenRequest 토큰 요청 파라미터
     * @return
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = tokenRequest.getRequestParameters();
        String authorizationCode = parameters.get("code");
        String redirectUri = parameters.get("redirect_uri");
        if (authorizationCode == null) {
            throw new InvalidRequestException("An authorization code must be supplied.");
        } else {
            String codeVerifier = parameters.getOrDefault("code_verifier", "");
            OAuth2Authentication oAuth2Authentication = pkceAuthorizationCodeService.consumeAuthorizationCodeAndCodeVerifier(authorizationCode, codeVerifier);
            if (oAuth2Authentication == null) {
                throw new InvalidGrantException("Invalid authorization code");
            } else {
                OAuth2Request oAuth2Request = oAuth2Authentication.getOAuth2Request();
                String redirectUriApprovalParameter = oAuth2Request.getRequestParameters().get("redirect_uri");
                if ((redirectUri != null || redirectUriApprovalParameter != null) && !oAuth2Request.getRedirectUri().equals(redirectUri)) {
                    throw new RedirectMismatchException("Redirect URI mismatch.");
                } else {
                    String storedClientId = oAuth2Request.getClientId();
                    String clientId = tokenRequest.getClientId();
                    if (clientId != null && !clientId.equals(storedClientId)) {
                        throw new InvalidClientException("Client ID mismatch.");
                    } else {
                        Map<String, String> combinedParameters = new HashMap<>(oAuth2Request.getRequestParameters());
                        combinedParameters.putAll(parameters);
                        OAuth2Request finalOauth2Request = oAuth2Request.createOAuth2Request(combinedParameters);
                        Authentication finalAuthentication = oAuth2Authentication.getUserAuthentication();
                        pkceAuthorizationCodeService.removeStoredAuthentication(authorizationCode);
                        return new OAuth2Authentication(finalOauth2Request, finalAuthentication);
                    }
                }
            }
        }
    }

}
