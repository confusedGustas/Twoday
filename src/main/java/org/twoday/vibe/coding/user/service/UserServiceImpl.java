package org.twoday.vibe.coding.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.auth.service.authentication.AuthenticationService;
import org.twoday.vibe.coding.user.dao.UserDao;
import org.twoday.vibe.coding.user.dto.UserResponseDto;
import org.twoday.vibe.coding.user.entity.User;
import org.twoday.vibe.coding.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

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

    private UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .verified(user.isVerified())
                .role(user.getRole())
                .build();
    }
}

