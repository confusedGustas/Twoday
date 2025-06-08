package org.twoday.vibe.coding.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twoday.vibe.coding.auth.dto.RoleAssignmentRequest;
import org.twoday.vibe.coding.auth.enums.UserRole;
import org.twoday.vibe.coding.user.entity.User;
import org.twoday.vibe.coding.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {
    private final UserRepository userRepository;

    @Transactional
    public void assignRole(RoleAssignmentRequest request, User currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only administrators can assign roles");
        }

        User targetUser = userRepository.findById(request.getUserIdAsUUID())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Prevent changing the last admin's role
        if (targetUser.getRole() == UserRole.ADMIN && request.getNewRole() != UserRole.ADMIN) {
            long adminCount = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == UserRole.ADMIN)
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot change the role of the last administrator");
            }
        }

        targetUser.setRole(request.getNewRole());
        userRepository.save(targetUser);
    }
} 