package org.twoday.vibe.coding.auth.service.authentication;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.auth.dto.LoginRequestDto;
import org.twoday.vibe.coding.auth.enums.UserRole;
import org.twoday.vibe.coding.auth.service.email.EmailVerificationService;
import org.twoday.vibe.coding.user.dao.UserDao;
import org.twoday.vibe.coding.user.entity.User;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserDao userDao;
    private final EmailVerificationService emailVerificationService;

    public void initiateLogin(LoginRequestDto loginRequestDto) {
        if (!emailVerificationService.isValidEmailFormat(loginRequestDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format. Must be name.surname@twoday.com");
        }

        User user = userDao.findByEmail(loginRequestDto.getEmail())
                .orElseGet(() -> createNewUser(loginRequestDto.getEmail()));

        emailVerificationService.sendVerificationEmail(user);
    }

    private User createNewUser(String email) {
        String[] nameParts = email.split("@")[0].split("\\.");
        User newUser = User.builder()
                .email(email)
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .verified(true)
                .role(UserRole.USER)
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
