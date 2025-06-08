package org.twoday.vibe.coding.user.service;

import org.twoday.vibe.coding.user.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUser();
    UserResponseDto getUserById(UUID id);
}
