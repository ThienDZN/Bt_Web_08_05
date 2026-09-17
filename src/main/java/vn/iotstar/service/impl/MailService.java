package vn.iotstar.service.impl;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import vn.iotstar.config.AppProperties;

public class MailService {
    public String sendOtp(String to, String recipientName, String otp, String purpose) {
        boolean mock = AppProperties.getBoolean("app.mail.mock", true);
        if (mock) {
            System.out.println("[MOCK MAIL] to=" + to + ", otp=" + otp + ", purpose=" + purpose);
            return "Mail is running in mock mode. Your OTP code is: " + otp;
        }

        String host = AppProperties.get("app.mail.host", "");
        String username = AppProperties.get("app.mail.username", "");
        String password = AppProperties.get("app.mail.password", "");
        String from = AppProperties.get("app.mail.from", username);
        String subject = "OTP verification";
        String content = "Hello " + recipientName + ", your OTP for " + purpose + " is: " + otp + ". It will expire in 5 minutes.";

        Properties props = new Properties();
        props.put("mail.smtp.auth", AppProperties.get("app.mail.auth", "true"));
        props.put("mail.smtp.starttls.enable", AppProperties.get("app.mail.starttls", "true"));
        props.put("mail.smtp.ssl.enable", AppProperties.get("app.mail.ssl", "false"));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", AppProperties.get("app.mail.port", "587"));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);
            Transport.send(message);
            return "The OTP has been sent to your email address.";
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to send the OTP email. Please check the mail settings in application.properties.", e);
        }
    }
}
