package csd.cuemaster.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


/**
 * Security configuration class for setting up web security in the application.
 * This class configures authentication providers, security filter chains, CORS settings, and more.
 * 
 * <p>It uses JWT for authentication and integrates with OAuth2 for Google login.</p>
 * 
 * <p>It also defines various security rules for different endpoints and HTTP methods.</p>
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    /**
     * Service to load user-specific data.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Filter for JWT authentication.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Google client ID for OAuth2 login.
     */
    @Value("${google.client-id}")
    private String clientId;

    /**
     * Google client secret for OAuth2 login.
     */
    @Value("${google.client-secret}")
    private String clientSecret;

    /**
     * URL of the client application.
     */
    @Value("${app.clientUrl}")
    private String clienturl;

    /**
     * Constructor to initialize the SecurityConfig with required services.
     * 
     * @param userDetailsService the service to load user-specific data
     * @param jwtAuthenticationFilter the filter for JWT authentication
     */
    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Bean for DaoAuthenticationProvider to handle authentication.
     * 
     * @return the DaoAuthenticationProvider bean
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }

    /**
     * Bean for configuring the security filter chain.
     * 
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Enable CORS support
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/normallogin/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/users", "/googlelogin/*", "/activate", "/activate/*",
                        "/loginSuccess", "/profiles", "/profile/*", "/tournaments/*", "/matches/*", "/matches",
                        "/tournaments", "/leaderboard", "/playerrank", "/userName/*","/user/*","/me").permitAll()
                .requestMatchers(HttpMethod.GET, "/googlelogin").permitAll()
                .requestMatchers(HttpMethod.POST, "/googlelogin").permitAll()
                .requestMatchers(HttpMethod.POST, "/activate").permitAll()

                .requestMatchers(HttpMethod.PUT, "/forgotPassword").permitAll()
                .requestMatchers(HttpMethod.PUT, "/resetPassword").permitAll()
                .requestMatchers(HttpMethod.POST, "/verify-code").permitAll()
                .requestMatchers(HttpMethod.POST, "/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/normallogin").permitAll()
                .requestMatchers(HttpMethod.GET, "/profilePhotos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/create/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/user/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/user/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/update/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/user/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/changepoints/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/playerstats/*").permitAll()
                .requestMatchers(HttpMethod.PUT, "/tournaments/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.POST, "/tournaments/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.DELETE, "/tournaments/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.POST, "/matches/create").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.DELETE, "/matches/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.PUT, "/matches/**").hasRole("ORGANISER")
                        .requestMatchers(HttpMethod.POST, "/matchmaking/*").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/googlelogin"))
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Adjust based on your needs
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions().disable()); 
        return http.build();
    }

    /**
     * Bean for BCryptPasswordEncoder to encode passwords.
     * 
     * @return the BCryptPasswordEncoder bean
     */
    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean for configuring CORS settings.
     * 
     * @return the CorsFilter bean
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList(clienturl)); // Your frontend origin
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }
}
