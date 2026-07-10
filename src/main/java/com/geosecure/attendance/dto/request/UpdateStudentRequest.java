package com.geosecure.attendance.dto.request;

import java.time.LocalDate;

public class UpdateStudentRequest {

    private String name;
    private Integer classId;
    private LocalDate dateOfBirth;
    private String phone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
