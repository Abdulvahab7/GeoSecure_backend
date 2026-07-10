package com.geosecure.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClassRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Department is required")
    private Integer departmentId;

    @NotNull(message = "Semester is required")
    private Integer semester;

    @NotBlank(message = "Section is required")
    private String section;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    /** Optional at creation time; a class may exist briefly with no coordinator. */
    private Integer coordinatorFacultyId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public Integer getCoordinatorFacultyId() { return coordinatorFacultyId; }
    public void setCoordinatorFacultyId(Integer coordinatorFacultyId) { this.coordinatorFacultyId = coordinatorFacultyId; }
}
