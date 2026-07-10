package com.geosecure.attendance.dto.request;

import com.geosecure.attendance.entity.Subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubjectRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Department is required")
    private Integer departmentId;

    @NotNull(message = "Semester is required")
    private Integer semester;

    private Integer credits = 3;
    private Subject.SubjectType subjectType = Subject.SubjectType.theory;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Subject.SubjectType getSubjectType() { return subjectType; }
    public void setSubjectType(Subject.SubjectType subjectType) { this.subjectType = subjectType; }
}
