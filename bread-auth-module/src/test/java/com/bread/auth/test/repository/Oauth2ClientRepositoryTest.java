package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataJpaTest;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.repository.Oauth2ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import static org.junit.jupiter.api.Assertions.*;

public class Oauth2ClientRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private Oauth2ClientRepository oauth2ClientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void findByClientId_Success() {
        // given
        String clientId = "test";
        // when
        Oauth2Client client = oauth2ClientRepository
                .findByClientId(clientId)
                .orElseThrow(() -> new NoSuchClientException(clientId));
        // then
        assertEquals(client.getClientId(), clientId);
        assertTrue(passwordEncoder.matches("1234", client.getClientSecret()));
        assertEquals(client.getAuthorizedGrantTypes(), "authorization_code,implicit,password,refresh_token");
        assertEquals(client.getScope(), "read,write");
        assertEquals(client.getAuthorities(), "user");
        assertEquals(client.getResourceIds(), "auth");
        assertEquals(client.getWebServerRedirectUri(), "http://localhost:9600");
    }

    @Test
    public void findByClientId_Fail() {
        // given
        String clientId = "invalid client Id";
        // when & then
        assertThrows(NoSuchClientException.class, () ->
                oauth2ClientRepository
                        .findByClientId(clientId)
                        .orElseThrow(() -> new NoSuchClientException(clientId))
        );
    }

    @Test
    public void save_Success() {
        // given
        String clientId = "mock";
        String clientSecret = "secret";
        String resourceIds = "auth";
        String scopes = "read,write";
        String grantTypes = "password,refresh_token";
        String authorities = "user";
        String redirectUri = "/";
        int tokenValidity = 1800;
        Oauth2Client expect = Oauth2Client
                .builder()
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .resourceIds(resourceIds)
                .scope(scopes)
                .authorizedGrantTypes(grantTypes)
                .authorities(authorities)
                .webServerRedirectUri(redirectUri)
                .accessTokenValidity(tokenValidity)
                .refreshTokenValidity(tokenValidity)
                .build();
        // when
        Oauth2Client save = oauth2ClientRepository.save(expect);
        // then
        assertEquals(clientId, save.getClientId());
        assertTrue(passwordEncoder.matches(clientSecret, save.getClientSecret()));
        assertEquals(resourceIds, save.getResourceIds());
        assertEquals(scopes, save.getScope());
        assertEquals(grantTypes, save.getAuthorizedGrantTypes());
        assertEquals(authorities, save.getAuthorities());
        assertEquals(redirectUri, save.getWebServerRedirectUri());
        assertEquals(tokenValidity, save.getAccessTokenValidity());
        assertEquals(tokenValidity, save.getRefreshTokenValidity());
    }

    @Test
    public void save_Fail_NotNull() {
        // given & when & then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> oauth2ClientRepository.save(
                        Oauth2Client
                                .builder()
                                .clientId("error")
                                .build()
                )
        );
    }

    @Test
    public void save_Fail_Unique() {
        // given
        String clientId = "mock";
        Oauth2Client expect = Oauth2Client
                .builder()
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode("secret"))
                .resourceIds("auth")
                .scope("read,write")
                .authorizedGrantTypes("password,refresh_token")
                .authorities("user")
                .webServerRedirectUri("/")
                .accessTokenValidity(1800)
                .refreshTokenValidity(1800)
                .build();
        Oauth2Client duplicate = Oauth2Client
                .builder()
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode("secret"))
                .resourceIds("auth")
                .scope("read,write")
                .authorizedGrantTypes("password,refresh_token")
                .authorities("user")
                .webServerRedirectUri("/")
                .accessTokenValidity(1800)
                .refreshTokenValidity(1800)
                .build();
        // when & then
        oauth2ClientRepository.save(expect);
        assertThrows(
                DataIntegrityViolationException.class,
                () -> oauth2ClientRepository.save(duplicate)
        );
    }

}
