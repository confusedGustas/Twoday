package org.twoday.vibe.coding.auth.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.auth.service.jwt.JwtService;
import org.twoday.vibe.coding.user.entity.User;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    
    private final JavaMailSender mailSender;
    private final JwtService jwtService;
    private static final String VERIFICATION_EMAIL_SUBJECT = "Verify your email";
    private static final String VERIFICATION_EMAIL_TEXT = "Please click the following link to verify your email: %s";
    private static final String VERIFICATION_BASE_URL = "http://localhost:5173/?token=";

    public void sendVerificationEmail(User user) {
        String jwtToken = jwtService.generateToken(user);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(VERIFICATION_EMAIL_SUBJECT);
        message.setText(String.format(VERIFICATION_EMAIL_TEXT, VERIFICATION_BASE_URL + jwtToken));
        mailSender.send(message);
    }

    public boolean isValidEmailFormat(String email) {
        if (email == null || !email.endsWith("@twoday.com")) {
            return false;
        }
        
        String[] parts = email.split("@")[0].split("\\.");
        return parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty();
    }
} 