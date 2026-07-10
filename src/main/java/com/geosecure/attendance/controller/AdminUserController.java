package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.CreateUserRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.UserResponse;
import com.geosecure.attendance.entity.Role;
import com.geosecure.attendance.service.UserService;

import jakarta.validation.Valid;

import com.geosecure.attendance.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll(@RequestParam(required = false) String role) {
        Role.RoleName roleEnum = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = Role.RoleName.valueOf(role.trim().toLowerCase());
            } catch (IllegalArgumentException e) {
                // Return empty list or ignore
            }
        }
        return ApiResponse.ok(userService.findAll(roleEnum));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(userService.findById(id));
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.create(request), "User created");
    }

    @GetMapping("/pending")
    public ApiResponse<List<UserResponse>> findPending(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.findPending(principal));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<UserResponse> approve(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.approve(id, principal), "Registration approved");
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<UserResponse> reject(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.reject(id, principal), "Registration rejected");
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<UserResponse> activate(@PathVariable Integer id) {
        return ApiResponse.ok(userService.setActive(id, true), "User activated");
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<UserResponse> deactivate(@PathVariable Integer id) {
        return ApiResponse.ok(userService.setActive(id, false), "User deactivated");
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("newPassword"));
        return ApiResponse.ok(null, "Password reset");
    }
}
