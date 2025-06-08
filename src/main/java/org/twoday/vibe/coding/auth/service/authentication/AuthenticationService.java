package org.twoday.vibe.coding.auth.service.authentication;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.auth.dto.LoginRequestDto;
import org.twoday.vibe.coding.auth.dto.LoginResponseDto;
import org.twoday.vibe.coding.auth.dto.RegisterRequestDto;
import org.twoday.vibe.coding.auth.mapper.AuthenticationMapper;
import org.twoday.vibe.coding.auth.service.jwt.JwtService;
import org.twoday.vibe.coding.user.dao.UserDao;
import org.twoday.vibe.coding.user.entity.User;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserDao userDao;
    private final AuthenticationMapper authenticationMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegisterRequestDto registerRequestDto) {
        var existingUser = userDao.findByEmail(registerRequestDto.getEmail());

        if (existingUser.isPresent()) {
            throw new EntityNotFoundException("User already exists");
        }

        userDao.saveUser(authenticationMapper.toUser(registerRequestDto));
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()));

        var user = userDao.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var jwtToken = jwtService.generateToken(user);

        return LoginResponseDto.builder()
                .token(jwtToken)
                .build();
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
