package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.ChangePasswordRequest;
import com.geosecure.attendance.dto.request.LoginRequest;
import com.geosecure.attendance.dto.request.RefreshTokenRequest;
import com.geosecure.attendance.dto.request.RegisterRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.AuthResponse;
import com.geosecure.attendance.dto.response.DepartmentResponse;
import com.geosecure.attendance.dto.response.UserResponse;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public endpoints (except /me and /change-password) - see SecurityConfig permitAll list. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        AuthResponse response = authService.login(request, clientIp(http), http.getHeader("User-Agent"));
        return ApiResponse.ok(response, "Login successful");
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok(null, "Registration submitted successfully. Await administrator approval.");
    }

    @GetMapping("/departments")
    public ApiResponse<List<DepartmentResponse>> departments() {
        return ApiResponse.ok(authService.listDepartments());
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request.getRefreshToken()));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.me(principal.getId()));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                             @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ApiResponse.ok(null, "Password changed successfully");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
