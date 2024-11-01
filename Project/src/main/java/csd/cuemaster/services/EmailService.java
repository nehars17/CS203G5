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
}
