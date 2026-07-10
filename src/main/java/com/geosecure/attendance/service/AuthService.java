package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.ChangePasswordRequest;
import com.geosecure.attendance.dto.request.LoginRequest;
import com.geosecure.attendance.dto.request.RegisterRequest;
import com.geosecure.attendance.dto.response.AuthResponse;
import com.geosecure.attendance.dto.response.DepartmentResponse;
import com.geosecure.attendance.dto.response.UserResponse;

import java.util.List;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    AuthResponse refresh(String refreshToken);

    UserResponse me(Integer userId);

    void changePassword(Integer userId, ChangePasswordRequest request);

    /** Public self-registration. Creates the user with status = PENDING; no tokens are issued. */
    void register(RegisterRequest request);

    /** Departments list for the public registration form's dropdown. */
    List<DepartmentResponse> listDepartments();
}
