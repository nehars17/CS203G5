package csd.cuemaster.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Generates a JWT token with the specified extra claims, user details, user ID, and role.
 *
 * @param extraClaims Additional claims to be included in the token.
 * @param userDetails The user details to be included in the token.
 * @param user_id The ID of the user to be included in the token.
 * @param role The role of the user to be included in the token.
 * @return The generated JWT token as a String.
 */

/**
 * Service class for handling JWT operations such as token generation, validation, and extraction of claims.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the given JWT token using the provided claims resolver function.
     *
     * @param token the JWT token
     * @param claimsResolver the function to resolve the claim
     * @param <T> the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for the given user details, user ID, and role.
     *
     * @param userDetails the user details
     * @param user_id the user ID
     * @param role the role of the user
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails, Long user_id, String role) {
        return generateToken(new HashMap<>(), userDetails, user_id, role);
    }

    /**
     * Generates a JWT token with additional claims for the given user details, user ID, and role.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @param user_id the user ID
     * @param role the role of the user
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, Long user_id, String role) {
        return buildToken(extraClaims, userDetails, jwtExpiration, user_id, role);
    }

    /**
     * Returns the expiration time for the JWT tokens.
     *
     * @return the expiration time in milliseconds
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Builds a JWT token with the given claims, user details, expiration time, user ID, and role.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @param expiration the expiration time in milliseconds
     * @param user_id the user ID
     * @param role the role of the user
     * @return the built JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration,
            Long user_id,
            String role
    ) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("sub", userDetails.getUsername());
        claims.put("user_id", user_id);
        claims.put("role", role);

        claims.put("iat", new Date(System.currentTimeMillis()));
        claims.put("exp", new Date(System.currentTimeMillis() + expiration));

        return Jwts
                .builder()
                .setClaims(claims)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the given JWT token against the provided user details.
     *
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the given JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from the given JWT token.
     *
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Returns the signing key used for signing the JWT tokens.
     *
     * @return the signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
