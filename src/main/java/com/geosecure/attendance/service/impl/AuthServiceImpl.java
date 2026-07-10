package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.ChangePasswordRequest;
import com.geosecure.attendance.dto.request.LoginRequest;
import com.geosecure.attendance.dto.request.RegisterRequest;
import com.geosecure.attendance.dto.response.AuthResponse;
import com.geosecure.attendance.dto.response.DepartmentResponse;
import com.geosecure.attendance.dto.response.UserResponse;
import com.geosecure.attendance.entity.Department;
import com.geosecure.attendance.entity.Role;
import com.geosecure.attendance.entity.User;
import com.geosecure.attendance.exception.BadRequestException;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.exception.UnauthorizedException;
import com.geosecure.attendance.repository.DepartmentRepository;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.repository.RoleRepository;
import com.geosecure.attendance.repository.StudentRepository;
import com.geosecure.attendance.repository.UserRepository;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.security.jwt.JwtTokenProvider;
import com.geosecure.attendance.service.AuditLogService;
import com.geosecure.attendance.service.AuthService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                            UserRepository userRepository,
                            RoleRepository roleRepository,
                            DepartmentRepository departmentRepository,
                            StudentRepository studentRepository,
                            FacultyRepository facultyRepository,
                            JwtTokenProvider tokenProvider,
                            PasswordEncoder passwordEncoder,
                            AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Registration-approval gate: checked BEFORE password verification so
        // an applicant always gets a clear status message rather than a
        // generic "invalid credentials" (or, for a correct password, silent
        // access before approval).
        User precheck = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (precheck != null) {
            boolean isAdmin = precheck.getRole() != null && precheck.getRole().getName() == Role.RoleName.admin;
            if (!isAdmin) {
                if (precheck.getStatus() == User.Status.PENDING) {
                    throw new UnauthorizedException("Your registration is awaiting administrator approval.");
                }
                if (precheck.getStatus() == User.Status.REJECTED) {
                    throw new UnauthorizedException("Your registration has been rejected.");
                }
                if (precheck.getStatus() == User.Status.DISABLED || !Boolean.TRUE.equals(precheck.getIsActive())) {
                    throw new UnauthorizedException("Your account has been disabled.");
                }
            }
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            throw new UnauthorizedException("Your account has been disabled.");
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(principal.getId());

        auditLogService.record(user.getId(), "LOGIN", "users", user.getId(), null, null, ipAddress, userAgent);

        return new AuthResponse(accessToken, refreshToken, UserResponse.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token is invalid or expired.");
        }
        Integer userId = tokenProvider.extractUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("This account has been deactivated.");
        }

        String role = user.getRole().getName().name();
        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), role);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        return new AuthResponse(newAccessToken, newRefreshToken, UserResponse.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.record(userId, "CHANGE_PASSWORD", "users", userId, null, null, null, null);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (request.getRole() == Role.RoleName.admin) {
            throw new BadRequestException("Admin registration is not permitted.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists.");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        User user = new User();
        user.setUsername(generateUniqueUsername(request.getEmail()));
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        // Not allowed to log in until an admin approves this application.
        user.setStatus(User.Status.PENDING);
        user.setIsActive(false);

        if (role.getName() == Role.RoleName.student) {
            if (isBlank(request.getRegisterNo())) {
                throw new BadRequestException("Register number is required for student registration.");
            }
            if (request.getDepartmentId() == null) {
                throw new BadRequestException("Department is required for student registration.");
            }
            if (request.getYear() == null) {
                throw new BadRequestException("Year is required for student registration.");
            }
            if (isBlank(request.getSection())) {
                throw new BadRequestException("Section is required for student registration.");
            }
            if (userRepository.existsByRegisterNo(request.getRegisterNo())
                    || studentRepository.existsByRegisterNo(request.getRegisterNo())) {
                throw new DuplicateResourceException(
                        "Register number '" + request.getRegisterNo() + "' is already in use.");
            }
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

            user.setRegisterNo(request.getRegisterNo());
            user.setDepartment(department);
            user.setYear(request.getYear());
            user.setSection(request.getSection());
        } else {
            if (isBlank(request.getEmployeeId())) {
                throw new BadRequestException("Employee ID is required for faculty registration.");
            }
            if (request.getDepartmentId() == null) {
                throw new BadRequestException("Department is required for faculty registration.");
            }
            if (userRepository.existsByEmployeeId(request.getEmployeeId())
                    || facultyRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new DuplicateResourceException(
                        "Employee ID '" + request.getEmployeeId() + "' is already in use.");
            }
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

            user.setEmployeeId(request.getEmployeeId());
            user.setDepartment(department);
            user.setDesignation(request.getDesignation());
        }

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    private String generateUniqueUsername(String email) {
        String base = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        base = base.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
        if (base.isBlank()) {
            base = "user";
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
