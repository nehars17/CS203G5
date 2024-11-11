package csd.cuemaster.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import csd.cuemaster.user.CustomAuthenticationSuccessHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler customSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    public SecurityConfig(UserDetailsService userDetailsService, CustomAuthenticationSuccessHandler customSuccessHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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
                .requestMatchers(HttpMethod.GET, "/users", "/googlelogin/*", "/activate", "/activate/*",
                        "/loginSuccess", "/profiles", "/profile/*", "/tournaments/*", "/matches/*", "/matches",
                        "/tournaments", "/leaderboard", "/playerrank", "/userName/*","/user/*","/me").permitAll()
                .requestMatchers(HttpMethod.GET, "/googlelogin").permitAll()
                .requestMatchers(HttpMethod.POST, "/googlelogin").permitAll()
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
                .loginPage("/googlelogin")
                .successHandler(customSuccessHandler))
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
