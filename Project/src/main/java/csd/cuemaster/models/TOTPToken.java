package csd.cuemaster.models;

import java.time.Instant;

/**
 * Represents a Time-based One-Time Password (TOTP) token.
 * This token contains a code and an expiration time.
 */
public class TOTPToken {
    private String code;
    private Instant expirationTime;

    // Constructors, getters, and setters
    public TOTPToken(String code, Instant expirationTime) {
        this.code = code;
        this.expirationTime = expirationTime;
    }

    public String getCode() {
        return code;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }
}
