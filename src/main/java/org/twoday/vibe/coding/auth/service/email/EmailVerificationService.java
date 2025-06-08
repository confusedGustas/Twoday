package org.twoday.vibe.coding.auth.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.user.entity.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    
    private final JavaMailSender mailSender;
    private static final String VERIFICATION_EMAIL_SUBJECT = "Verify your email";
    private static final String VERIFICATION_EMAIL_TEXT = "Please click the following link to verify your email: %s";
    private static final String VERIFICATION_BASE_URL = "http://localhost:8080/auth/verify?token=";
    
    // In-memory token store - in production, this should be replaced with a database
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public void sendVerificationEmail(User user, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(VERIFICATION_EMAIL_SUBJECT);
        message.setText(String.format(VERIFICATION_EMAIL_TEXT, VERIFICATION_BASE_URL + token));
        mailSender.send(message);
        
        // Store the token with the user's email
        tokenStore.put(token, user.getEmail());
    }

    public boolean isValidEmailFormat(String email) {
        if (email == null || !email.endsWith("@twoday.com")) {
            return false;
        }
        
        String[] parts = email.split("@")[0].split("\\.");
        return parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty();
    }

    public String getEmailForToken(String token) {
        return tokenStore.remove(token); // Remove the token after use
    }
} 