package org.twoday.vibe.coding.auth.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twoday.vibe.coding.auth.dto.LoginRequestDto;
import org.twoday.vibe.coding.auth.dto.LoginResponseDto;
import org.twoday.vibe.coding.auth.dto.RegisterRequestDto;
import org.twoday.vibe.coding.auth.service.authentication.AuthenticationService;

@RequestMapping("/auth")
@RestController
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        authenticationService.register(registerRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authenticationService.login(loginRequestDto));
    }

}
