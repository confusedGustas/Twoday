package org.twoday.vibe.coding.user.dto;

import lombok.*;

import java.time.LocalDate;
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
    private LocalDate birthDate;
}
