package csd.cuemaster.services;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.KeyGenerator;

import org.springframework.stereotype.Service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import csd.cuemaster.models.TOTPToken;

/**
 * Service for generating and validating Time-based One-Time Passwords (TOTP).
 */
@Service
public class TOTPService {
    private TimeBasedOneTimePasswordGenerator totp;
    /**
     * Constructs a new TOTPService with a TimeBasedOneTimePasswordGenerator.
     *
     * @throws Exception if an error occurs during the creation of the TOTP generator.
     */
    public TOTPService() throws Exception {
        this.totp = new TimeBasedOneTimePasswordGenerator();
    }

    /**
     * Generates a new secret key for TOTP.
     *
     * @return the generated secret key.
     * @throws Exception if an error occurs during key generation.
     */
    public Key generateSecret() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        keyGenerator.init(160);
        return keyGenerator.generateKey();
    }

    /**
     * Generates a TOTP token using the provided secret key.
     *
     * @param secret the secret key used to generate the TOTP token.
     * @return the generated TOTP token.
     * @throws Exception if an error occurs during token generation.
     */
    public TOTPToken generateTOTPToken(Key secret) throws Exception {
        String code = String.valueOf(totp.generateOneTimePassword(secret, Instant.now()));
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5)); // Token valid for 5 minutes
        return new TOTPToken(code, expirationTime);
    }

    /**
     * Validates a TOTP token using the provided secret key.
     *
     * @param secret the secret key used to validate the TOTP token.
     * @param token the TOTP token to be validated.
     * @return true if the token is valid, false otherwise.
     * @throws Exception if an error occurs during token validation.
     */
    public boolean validateTOTPToken(Key secret, TOTPToken token) throws Exception {
        if (token.getExpirationTime().isBefore(Instant.now())) {
            return false; // Token has expired
        }

        Instant now = Instant.now();
        long otpCurrent = totp.generateOneTimePassword(secret, now);
        long otpPrevious = totp.generateOneTimePassword(secret, now.minus(totp.getTimeStep()));
        long otpNext = totp.generateOneTimePassword(secret, now.plus(totp.getTimeStep()));

        return String.valueOf(otpCurrent).equals(token.getCode()) ||
                String.valueOf(otpPrevious).equals(token.getCode()) ||
                String.valueOf(otpNext).equals(token.getCode());
    }

}
