package org.twoday.vibe.coding.user.service;


import org.twoday.vibe.coding.user.dto.UserResponseDto;

import java.util.UUID;

public interface UserService {
    UserResponseDto getUser();
    UserResponseDto getUserById(UUID id);

}
