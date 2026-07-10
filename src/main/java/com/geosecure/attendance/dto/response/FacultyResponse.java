package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.Faculty;

public class FacultyResponse {

    private Integer id;
    private Integer userId;
    private String employeeId;
    private String name;
    private String email;
    private Integer departmentId;
    private String departmentName;
    private String designation;
    private String phone;
    private Boolean isMentor;
    private String photoUrl;

    public static FacultyResponse from(Faculty f) {
        FacultyResponse r = new FacultyResponse();
        r.id = f.getId();
        r.userId = f.getUser() != null ? f.getUser().getId() : null;
        r.employeeId = f.getEmployeeId();
        r.name = f.getName();
        r.email = f.getUser() != null ? f.getUser().getEmail() : null;
        r.departmentId = f.getDepartment() != null ? f.getDepartment().getId() : null;
        r.departmentName = f.getDepartment() != null ? f.getDepartment().getName() : null;
        r.designation = f.getDesignation();
        r.phone = f.getPhone();
        r.isMentor = f.getIsMentor();
        r.photoUrl = f.getPhotoUrl();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getIsMentor() { return isMentor; }
    public void setIsMentor(Boolean isMentor) { this.isMentor = isMentor; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
