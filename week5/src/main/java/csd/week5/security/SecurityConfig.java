package csd.week5.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    
    private UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userSvc){
        this.userDetailsService = userSvc;
    }
    
    /**
     * Exposes a bean of DaoAuthenticationProvider, a type of AuthenticationProvider
     * Attaches the user details and the password encoder   
     * @return
     */

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
     
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
 
        return authProvider;
    }

    /**
     * TODO: Activity 2a - Authentication
     * Add code to secure requests to Reviews
     * In particular, only authenticated users would be able to create/update/delete Reviews
     * Hint: Add requestMatchers rules
     
   
     * TODO: Activity 2b - Authorization
     * Add roles to specify permissions for each enpoint
     * User role: can add review.
     * Admin role: can add/delete/update books/reviews, and add/list users
     *  
     * Note: '*' matches zero or more characters, e.g., /books/* matches /books/20
             '**' matches zero or more 'directories' in a path, e.g., /books/** matches /books/1/reviews 
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // example: get the username of logged-in user
        http
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/error").permitAll() // the default error page
                .requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/books").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/books/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/books/*").authenticated()
                // note that Spring Security 6 secures all endpoints by default
                // remove the below line after adding the required rules
                .requestMatchers(HttpMethod.POST, "/books/*/reviews").hasAnyRole("ADMIN","USER")
                .requestMatchers(HttpMethod.PUT, "/books/*/reviews/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/books/*/reviews/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                /* .anyRequest().permitAll() all other requests are allowed */
            )
            // ensure that the application won’t create any session in our stateless REST APIs
            .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable()) // CSRF protection is needed only for browser based attacks
            .formLogin(form -> form.disable())
            .headers(header -> header.disable()) // disable the security headers, as we do not return HTML in our APIs
            .authenticationProvider(authenticationProvider());
        
        return http.build();
    }



    /**
     * @Bean annotation is used to declare a PasswordEncoder bean in the Spring application context. 
     * Any calls to encoder() will then be intercepted to return the bean instance.
     */
    @Bean
    public BCryptPasswordEncoder encoder() {
        // auto-generate a random salt internally
        return new BCryptPasswordEncoder();
    }
}
 