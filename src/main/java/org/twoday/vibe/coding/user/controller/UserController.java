package org.twoday.vibe.coding.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twoday.vibe.coding.user.dto.ChangePasswordRequestDto;
import org.twoday.vibe.coding.user.dto.UserResponseDto;
import org.twoday.vibe.coding.user.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto getUserById(){
        return userService.getUser();
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }

}
