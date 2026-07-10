package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.CreateUserRequest;
import com.geosecure.attendance.dto.response.UserResponse;
import com.geosecure.attendance.entity.Role;
import com.geosecure.attendance.entity.User;
import com.geosecure.attendance.exception.BadRequestException;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.RoleRepository;
import com.geosecure.attendance.repository.UserRepository;
import com.geosecure.attendance.service.UserService;

import com.geosecure.attendance.exception.ForbiddenException;
import com.geosecure.attendance.security.UserPrincipal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll(Role.RoleName roleFilter) {
        return userRepository.findAllByOptionalRole(roleFilter).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Integer id) {
        return UserResponse.from(requireById(id));
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken.");
        }
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse setActive(Integer id, boolean active) {
        User user = requireById(id);
        user.setIsActive(active);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void resetPassword(Integer id, String newPassword) {
        User user = requireById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(Role.RoleName role) {
        return userRepository.countByRole_Name(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findPending(UserPrincipal caller) {
        List<User> allPending = userRepository.findAllByStatusOrderByCreatedAtAsc(User.Status.PENDING);
        String callerRole = caller.getRole().toLowerCase();
        if ("admin".equals(callerRole)) {
            return allPending.stream()
                    .filter(u -> u.getRole().getName() == Role.RoleName.faculty || u.getRole().getName() == Role.RoleName.student)
                    .map(UserResponse::from)
                    .toList();
        } else if ("faculty".equals(callerRole)) {
            return allPending.stream()
                    .filter(u -> u.getRole().getName() == Role.RoleName.student)
                    .map(UserResponse::from)
                    .toList();
        } else {
            return List.of();
        }
    }

    @Override
    @Transactional
    public UserResponse approve(Integer id, UserPrincipal caller) {
        User user = requireById(id);
        if (user.getStatus() != User.Status.PENDING) {
            throw new BadRequestException("Only pending registrations can be approved.");
        }

        String callerRole = caller.getRole().toLowerCase();
        if ("admin".equals(callerRole)) {
            if (user.getRole().getName() != Role.RoleName.faculty && user.getRole().getName() != Role.RoleName.student) {
                throw new BadRequestException("Admin is only allowed to approve faculty and student accounts.");
            }
        } else if ("faculty".equals(callerRole)) {
            if (user.getRole().getName() != Role.RoleName.student) {
                throw new BadRequestException("Faculty is only allowed to approve student accounts.");
            }
        } else {
            throw new ForbiddenException("You are not authorized to approve accounts.");
        }

        user.setStatus(User.Status.APPROVED);
        user.setIsActive(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse reject(Integer id, UserPrincipal caller) {
        User user = requireById(id);
        if (user.getStatus() != User.Status.PENDING) {
            throw new BadRequestException("Only pending registrations can be rejected.");
        }

        String callerRole = caller.getRole().toLowerCase();
        if ("admin".equals(callerRole)) {
            if (user.getRole().getName() != Role.RoleName.faculty && user.getRole().getName() != Role.RoleName.student) {
                throw new BadRequestException("Admin is only allowed to reject faculty and student accounts.");
            }
        } else if ("faculty".equals(callerRole)) {
            if (user.getRole().getName() != Role.RoleName.student) {
                throw new BadRequestException("Faculty is only allowed to reject student accounts.");
            }
        } else {
            throw new ForbiddenException("You are not authorized to reject accounts.");
        }

        user.setStatus(User.Status.REJECTED);
        user.setIsActive(false);
        return UserResponse.from(userRepository.save(user));
    }

    private User requireById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
