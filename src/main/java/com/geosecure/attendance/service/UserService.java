package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.CreateUserRequest;
import com.geosecure.attendance.dto.response.UserResponse;
import com.geosecure.attendance.entity.Role;

import com.geosecure.attendance.security.UserPrincipal;

import java.util.List;

public interface UserService {

    List<UserResponse> findAll(Role.RoleName roleFilter);

    UserResponse findById(Integer id);

    UserResponse create(CreateUserRequest request);

    UserResponse setActive(Integer id, boolean active);

    void resetPassword(Integer id, String newPassword);

    long countByRole(Role.RoleName role);

    /** Registration approval workflow. */
    List<UserResponse> findPending(UserPrincipal caller);

    UserResponse approve(Integer id, UserPrincipal caller);

    UserResponse reject(Integer id, UserPrincipal caller);
}
