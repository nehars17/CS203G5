package csd.cuemaster.services;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.KeyGenerator;

import org.springframework.stereotype.Service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import csd.cuemaster.models.TOTPToken;


@Service
public class TOTPService {

    private final TimeBasedOneTimePasswordGenerator totp;

    public TOTPService() throws Exception {
        this.totp = new TimeBasedOneTimePasswordGenerator();
    }

    public Key generateSecret() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        keyGenerator.init(160);
        return keyGenerator.generateKey();
    }


    public TOTPToken generateTOTPToken(Key secret) throws Exception {
        String code = String.valueOf(totp.generateOneTimePassword(secret, Instant.now()));
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5)); // Token valid for 5 minutes
        return new TOTPToken(code, expirationTime);
    }

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
