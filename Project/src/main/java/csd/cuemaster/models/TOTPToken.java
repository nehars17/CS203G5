package csd.cuemaster.models;

import java.time.Instant;

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
