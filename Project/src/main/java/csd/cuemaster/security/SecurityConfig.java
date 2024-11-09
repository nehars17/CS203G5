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


@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Enable CORS support
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/normallogin/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/users", "/googlelogin/*", "/activate/*",
                        "/loginSuccess", "/profiles", "/user/**", "/tournaments/*", "/matches/*", "/matches",
                        "/tournaments", "/leaderboard", "/me","/forgotPassword/*","/resetPassword/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/googlelogin").permitAll()
                .requestMatchers(HttpMethod.POST, "/googlelogin").permitAll()
                .requestMatchers(HttpMethod.POST, "/activate").permitAll()

                .requestMatchers(HttpMethod.PUT, "/forgotPassword").permitAll()
                .requestMatchers(HttpMethod.PUT, "/resetPassword").permitAll()
                .requestMatchers(HttpMethod.POST, "/verify-code").permitAll()
                .requestMatchers(HttpMethod.POST, "/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/normallogin").permitAll()
                .requestMatchers(HttpMethod.POST, "/user/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/user/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/changepoints/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/user/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/tournaments/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.POST, "/tournaments/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.DELETE, "/tournaments/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.POST, "/matches/create").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.DELETE, "/matches/*").hasRole("ORGANISER")
                .requestMatchers(HttpMethod.PUT, "/matches/**").hasRole("ORGANISER")
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

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // Your frontend origin
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }
}
