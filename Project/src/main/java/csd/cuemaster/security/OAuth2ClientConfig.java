package csd.cuemaster.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

/**
 * Configuration class for setting up OAuth2 client registration with Google.
 * This class reads the client ID and client secret from the application properties
 * and configures the client registration repository with Google OAuth2 details.
 * 
 * <p>It uses the following properties:
 * <ul>
 *   <li><code>google.client-id</code>: The client ID for the Google OAuth2 application.</li>
 *   <li><code>google.client-secret</code>: The client secret for the Google OAuth2 application.</li>
 * </ul>
 * 
 * <p>The client registration is configured with the following details:
 * <ul>
 *   <li>Client Authentication Method: CLIENT_SECRET_BASIC</li>
 *   <li>Authorization Grant Type: AUTHORIZATION_CODE</li>
 *   <li>Redirect URI: {baseUrl}/login/oauth2/code/{registrationId}</li>
 *   <li>Scopes: openid, profile, email</li>
 *   <li>Authorization URI: https://accounts.google.com/o/oauth2/v2/auth</li>
 *   <li>Token URI: https://www.googleapis.com/oauth2/v4/token</li>
 *   <li>User Info URI: https://www.googleapis.com/oauth2/v3/userinfo</li>
 *   <li>User Name Attribute: sub</li>
 *   <li>JWK Set URI: https://www.googleapis.com/oauth2/v3/certs</li>
 *   <li>Client Name: Google</li>
 * </ul>
 * 
 * <p>This configuration is used to enable OAuth2 login with Google in a Spring Boot application.
 * 
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.beans.factory.annotation.Value
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.security.oauth2.client.registration.ClientRegistration
 * @see org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
 * @see org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
 * @see org.springframework.security.oauth2.core.ClientAuthenticationMethod
 * @see org.springframework.security.oauth2.core.AuthorizationGrantType
 * @see org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
 * @author Neha
 */
@Configuration
public class OAuth2ClientConfig {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(googleClientRegistration());
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }
}
