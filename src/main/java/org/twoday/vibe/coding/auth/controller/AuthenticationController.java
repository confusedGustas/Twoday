package org.twoday.vibe.coding.auth.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twoday.vibe.coding.auth.dto.LoginRequestDto;
import org.twoday.vibe.coding.auth.dto.LoginResponseDto;
import org.twoday.vibe.coding.auth.dto.RegisterRequestDto;
import org.twoday.vibe.coding.auth.service.authentication.AuthenticationService;

@RequestMapping("/auth")
@RestController
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<Void> initiateLogin(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        authenticationService.initiateLogin(loginRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<LoginResponseDto> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authenticationService.verifyEmail(token));
    }

}
