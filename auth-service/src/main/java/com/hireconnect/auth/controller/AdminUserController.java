package com.hireconnect.auth.controller;

import com.hireconnect.auth.dto.UserResponse;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserStatus;
import com.hireconnect.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AuthService authService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(@RequestParam(required = false) Role role) {
        log.info("Admin request to get all users with role: {}", role);
        return ResponseEntity.ok(authService.getAllUsers(role));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> statusRequest) {
        String statusStr = statusRequest.get("status");
        UserStatus status = UserStatus.valueOf(statusStr.toUpperCase());
        log.info("Admin request to update user {} status to {}", userId, status);
        authService.updateUserStatus(userId, status);
        return ResponseEntity.ok().build();
    }
}
