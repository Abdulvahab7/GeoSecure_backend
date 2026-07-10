package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.User;

import java.time.LocalDateTime;

public class UserResponse {

    private Integer id;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // ---- Registration approval workflow (additive; null for pre-existing/admin-created users) ----
    private String status;
    private String fullName;
    private String phone;
    private String departmentName;
    private String registerNo;
    private Integer year;
    private String section;
    private String employeeId;
    private String designation;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.username = u.getUsername();
        r.email = u.getEmail();
        r.role = u.getRole().getName().name().toUpperCase();
        r.isActive = u.getIsActive();
        r.lastLogin = u.getLastLogin();
        r.createdAt = u.getCreatedAt();
        r.status = u.getStatus() != null ? u.getStatus().name() : null;
        r.fullName = u.getFullName();
        r.phone = u.getPhone();
        r.departmentName = u.getDepartment() != null ? u.getDepartment().getName() : null;
        r.registerNo = u.getRegisterNo();
        r.year = u.getYear();
        r.section = u.getSection();
        r.employeeId = u.getEmployeeId();
        r.designation = u.getDesignation();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getRegisterNo() { return registerNo; }
    public void setRegisterNo(String registerNo) { this.registerNo = registerNo; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
