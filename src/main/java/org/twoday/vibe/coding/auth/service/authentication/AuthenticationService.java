package org.twoday.vibe.coding.auth.service.authentication;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.auth.dto.LoginRequestDto;
import org.twoday.vibe.coding.auth.dto.LoginResponseDto;
import org.twoday.vibe.coding.auth.service.email.EmailVerificationService;
import org.twoday.vibe.coding.auth.service.jwt.JwtService;
import org.twoday.vibe.coding.user.dao.UserDao;
import org.twoday.vibe.coding.user.entity.User;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserDao userDao;
    private final EmailVerificationService emailVerificationService;
    private final JwtService jwtService;

    public void initiateLogin(LoginRequestDto loginRequestDto) {
        if (!emailVerificationService.isValidEmailFormat(loginRequestDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format. Must be name.surname@twoday.com");
        }

        User user = userDao.findByEmail(loginRequestDto.getEmail())
                .orElseGet(() -> createNewUser(loginRequestDto.getEmail()));

        String verificationToken = UUID.randomUUID().toString();
        emailVerificationService.sendVerificationEmail(user, verificationToken);
    }

    public LoginResponseDto verifyEmail(String token) {
        String email = emailVerificationService.getEmailForToken(token);
        if (email == null) {
            throw new EntityNotFoundException("Invalid or expired verification token");
        }

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setVerified(true);
        userDao.saveUser(user);

        String jwtToken = jwtService.generateToken(user);
        return LoginResponseDto.builder()
                .token(jwtToken)
                .build();
    }

    private User createNewUser(String email) {
        String[] nameParts = email.split("@")[0].split("\\.");
        User newUser = User.builder()
                .email(email)
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .verified(false)
                .build();
        return userDao.saveUser(newUser);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()) {
            throw new EntityNotFoundException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new EntityNotFoundException("Authenticated principal is not a User.");
        }

        return user;
    }
}
