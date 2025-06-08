package org.twoday.vibe.coding.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twoday.vibe.coding.auth.dto.RoleAssignmentRequest;
import org.twoday.vibe.coding.auth.service.RoleAssignmentService;
import org.twoday.vibe.coding.user.entity.User;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleAssignmentController {
    private final RoleAssignmentService roleAssignmentService;

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRole(
            @Valid @RequestBody RoleAssignmentRequest request,
            @AuthenticationPrincipal User currentUser) {
        roleAssignmentService.assignRole(request, currentUser);
        return ResponseEntity.ok().build();
    }
} 