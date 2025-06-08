package org.twoday.vibe.coding.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twoday.vibe.coding.auth.dto.RoleAssignmentRequest;
import org.twoday.vibe.coding.auth.enums.UserRole;
import org.twoday.vibe.coding.user.entity.User;
import org.twoday.vibe.coding.user.repository.UserRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {
    private final UserRepository userRepository;
    private static final Set<UserRole> ASSIGNABLE_ROLES = Set.of(
            UserRole.COACH,
            UserRole.COMMITTEE_LEAD,
            UserRole.DIRECTOR
    );

    @Transactional
    public void assignRole(RoleAssignmentRequest request, User currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only administrators can assign roles");
        }

        if (!ASSIGNABLE_ROLES.contains(request.getNewRole())) {
            throw new IllegalArgumentException("Invalid role assignment. Only COACH, COMMITTEE_LEAD, and DIRECTOR roles can be assigned.");
        }

        User targetUser = userRepository.findById(request.getUserIdAsUUID())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        targetUser.setRole(request.getNewRole());
        userRepository.save(targetUser);
    }
} 