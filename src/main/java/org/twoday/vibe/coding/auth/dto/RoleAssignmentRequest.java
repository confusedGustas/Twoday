package org.twoday.vibe.coding.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.twoday.vibe.coding.auth.enums.UserRole;

import java.util.UUID;

@Data
public class RoleAssignmentRequest {
    @NotNull
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", 
            message = "Invalid UUID format. Must be in format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
    private String userId;
    
    @NotNull
    private UserRole newRole;

    public UUID getUserIdAsUUID() {
        return UUID.fromString(userId);
    }
} 