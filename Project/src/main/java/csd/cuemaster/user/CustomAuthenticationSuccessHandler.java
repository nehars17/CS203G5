package csd.cuemaster.user; // Use your actual package name

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // Register this as a Spring bean
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Log the authentication details for debugging
        System.out.println("Login successful! User: " + authentication.getPrincipal());

        // Optionally, store relevant details in the session if needed
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            // Extract user attributes
            String email = oAuth2User.getAttribute("email"); // Adjust based on provider's response
            String name = oAuth2User.getAttribute("name");
            // You can access other fields similarly
            System.out.println("Email: " + email);
            System.out.println("Name: " + name);

            // Optionally, store relevant details in the session if needed
            request.getSession().setAttribute("userEmail", email);
            request.getSession().setAttribute("userName", name);
        } else {
            throw new IllegalArgumentException("Principal is not an instance of OAuth2User");
        }

        // Redirect to the desired URL
        response.sendRedirect("/loginSuccess");
    }
}
