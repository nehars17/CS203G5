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
             '**' matches zero or more 'directories' in a path, e.g., /books/** matches /books/1/reviews 
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                // .requestMatchers("/error").permitAll() // the default error page
                // .requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll()
                // .requestMatchers(HttpMethod.POST, "/books").authenticated()
                // .requestMatchers(HttpMethod.PUT, "/books/*").hasAnyRole("ADMIN")
                // .requestMatchers(HttpMethod.DELETE, "/books/*").hasAnyRole("ADMIN")
                // note that Spring Security 6 secures all endpoints by default
                // remove the below line after adding the required rules
                .anyRequest().permitAll() // allowing the request without any authentication / authorization 
                // .requestMatchers(HttpMethod.POST, "/books/*/reviews").authenticated()
                // .requestMatchers(HttpMethod.DELETE, "/books/*/reviews/*").hasAnyRole("ADMIN")
                // .requestMatchers(HttpMethod.PUT, "/books/*/reviews").hasAnyRole("ADMIN")
                // .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("ADMIN")
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
 