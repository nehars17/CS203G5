package csd.cuemaster.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;

import csd.cuemaster.user.CustomAuthenticationSuccessHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private UserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler customSuccessHandler; // Add this line

    public SecurityConfig(UserDetailsService userSvc, CustomAuthenticationSuccessHandler customSuccessHandler) {
        this.userDetailsService = userSvc;
        this.customSuccessHandler = customSuccessHandler; // Assign it here
    }

    /**
     * Exposes a bean of DaoAuthenticationProvider, a type of AuthenticationProvider
     * Attaches the user details and the password encoder
     * 
     * @return
     */

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.googleClientRegistration());
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("643146770456-qeusj14u53puh4bi1hhi2t8g21qhh1hc.apps.googleusercontent.com")
                .clientSecret("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")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email", "address", "phone")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }

    /**
     * TODO: Activity 2a - Authentication
     * Add code to secure requests to Reviews
     * In particular, only authenticated users would be able to create/update/delete
     * Reviews
     * Hint: Add requestMatchers rules
     * 
     * 
     * 
     * 
     * TODO: Activity 2b - Authorization
     * Add roles to specify permissions for each enpoint
     * User role: can add review.
     * Admin role: can add/delete/update books/reviews, and add/list users
     * 
     * Note: '*' matches zero or more characters, e.g., /books/* matches /books/20
     * '**' matches zero or more 'directories' in a path, e.g., /books/** matches
     * /books/1/reviews
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET,"/users", "googlelogin/*", "/activate","/activate/*","/normallogin", "/loginSuccess","/profiles", "/user/**","/tournaments/*","/matches/*", "/matches","/tournaments").permitAll()
                        .requestMatchers(HttpMethod.POST,"/register/*").permitAll()
                        .requestMatchers(HttpMethod.POST,"/user/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/user/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/user/**").authenticated()
                        // .requestMatchers(HttpMethod.GET,"/tournaments/*").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/tournaments/*").hasRole("ORGANISER")
                        .requestMatchers(HttpMethod.POST,"/tournaments/*").hasRole("ORGANISER")
                        .requestMatchers(HttpMethod.DELETE,"/tournaments/*").hasRole("ORGANISER")

                        // .requestMatchers(HttpMethod.GET,"/matches/*").permitAll()
                        .requestMatchers(HttpMethod.POST,"/matches/create").permitAll()
                        .requestMatchers(HttpMethod.DELETE,"/matches/*").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/matches/*").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/normallogin").permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/googlelogin/*")
                        .successHandler(customSuccessHandler)

             

                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER) // Stateless session management for REST
                                                                                // APIs

                )
                .sessionManagement(session -> session
                        .maximumSessions(1) // Limit to one session per user
                        .maxSessionsPreventsLogin(true) // Prevent new login if session exists
                )

                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // CSRF protection is needed only for browser based attacks
                .headers(header -> header.disable()) // disable the security headers, as we do not return HTML in our
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * @Bean annotation is used to declare a PasswordEncoder bean in the Spring
     *       application context.
     *       Any calls to encoder() will then be intercepted to return the bean
     *       instance.
     */
    @Bean
    public BCryptPasswordEncoder encoder() {
        // auto-generate a random salt internally
        return new BCryptPasswordEncoder();
    }

}