package org.twoday.vibe.coding.auth.mapper;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.twoday.vibe.coding.auth.dto.RegisterRequestDto;
import org.twoday.vibe.coding.user.entity.User;

@Configuration
@AllArgsConstructor
public class AuthenticationMapper {

    private final PasswordEncoder passwordEncoder;

    public User toUser(RegisterRequestDto registerRequest) {
        return User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getName())
                .lastName(registerRequest.getSurname())
                .birthDate(registerRequest.getBirthDate())
                .build();
    }

}
