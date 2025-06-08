package org.twoday.vibe.coding.user.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.auth.service.authentication.AuthenticationService;
import org.twoday.vibe.coding.user.dao.UserDao;
import org.twoday.vibe.coding.user.dto.ChangePasswordRequestDto;
import org.twoday.vibe.coding.user.dto.UserResponseDto;
import org.twoday.vibe.coding.user.entity.User;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @Override
    public UserResponseDto getUser() {
        User user = authenticationService.getAuthenticatedUser();

        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public UserResponseDto getUserById(UUID userId) {
        Optional<User> optionalUser = userDao.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = optionalUser.get();

        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

}

