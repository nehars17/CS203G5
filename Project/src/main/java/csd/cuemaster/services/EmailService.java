package csd.cuemaster.services;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String recipientEmail, String link) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        
        helper.setTo(recipientEmail);
        helper.setSubject("Account Activation");
        helper.setText("<p>Please click the link below to activate your account:</p>"
                + "<a href=\"" + link + "\">Activate Now</a>", true);

        mailSender.send(message);
    }

    public void send2FAEmail(String recipientEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        
        helper.setTo(recipientEmail);
        helper.setSubject("2FA Authentication");
        helper.setText("<p>Here is your 2FA Code :</p>"
                + code, true);

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String username, Long userId, String token) throws Exception {
   
        // Store the token and expiration time in the database if needed
        // For demonstration, this step is omitted
    
        // Create the reset URL
        String resetUrl = "http://localhost:3000/resetPassword"+"?token=" + token +"&id="+userId;
        
        // Create an email message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(username);
        message.setSubject("Password Reset Request");
        message.setText("Hello " + username + ",\n\nTo reset your password, please click the link below:\n" + resetUrl + "\n\nIf you did not request a password reset, please ignore this email.\n\nThank you.");
        
        // Send the email
        mailSender.send(message);
    
    }
    
       
}
