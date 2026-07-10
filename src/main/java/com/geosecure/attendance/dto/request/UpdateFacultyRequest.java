package com.geosecure.attendance.dto.request;

public class UpdateFacultyRequest {

    private String name;
    private Integer departmentId;
    private String designation;
    private String phone;
    private Boolean isMentor;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getIsMentor() { return isMentor; }
    public void setIsMentor(Boolean isMentor) { this.isMentor = isMentor; }
}
