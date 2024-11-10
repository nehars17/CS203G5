package csd.cuemaster.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service class for sending various types of emails such as activation emails, 
 * 2FA codes, and password reset links.
 */
@Service
public class EmailService {
    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Value("${app.clientUrl}")
    private String clientUrl;

    /**
     * Sends an email to the specified recipient with the activation link.
     *
     * @param recipientEmail The email address of the recipient.
     * @param link           The activation link to be included in the email.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public void sendActivationEmail(String recipientEmail, String code) throws MessagingException {

        String activationLink = clientUrl+"/activateaccount?token=" + code;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(recipientEmail);
        helper.setSubject("Account Activation");
        helper.setText("<p>Please click the link below to activate your account:</p>"
                + "<a href=\"" + activationLink + "\">Activate Now</a>", true);

        mailSender.send(message);
    }

    public void sendUnlockedEmail(String recipientEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(recipientEmail);
        helper.setSubject("Account Unlocked");
        helper.setText("<p>Your account has been unlocked. You can now login.</p>", true);

        mailSender.send(message);
    }
    /*
     * Sends an email to the specified recipient with the 2FA code.
     */
    public void send2FAEmail(String recipientEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(recipientEmail);
        helper.setSubject("2FA Authentication");
        helper.setText("<p>Here is your 2FA Code :</p>"
                + code, true);

        mailSender.send(message);
    }

    /*
     * Sends an email to the specified recipient with the password reset link.
     */

    public void sendPasswordResetEmail(String username, Long userId, String token) throws Exception {


        // Create the reset URL
        String resetUrl = clientUrl+"/resetPassword" + "?token=" + token + "&id=" + userId;

        // Create an email message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(username);
        message.setSubject("Password Reset Request");
        message.setText("Hello " + username + ",\n\nTo reset your password, please click the link below:\n" + resetUrl
                + "\n\nIf you did not request a password reset, please ignore this email.\n\nThank you.");

        // Send the email
        mailSender.send(message);

    }

}
