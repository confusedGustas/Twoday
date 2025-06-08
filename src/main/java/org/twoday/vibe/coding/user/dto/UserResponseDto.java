package org.twoday.vibe.coding.user.dto;

import lombok.*;
import org.twoday.vibe.coding.auth.enums.UserRole;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean verified;
    private UserRole role;
}
