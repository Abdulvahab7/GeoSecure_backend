package com.geosecure.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Assigns "this faculty teaches this subject to this class this year." */
public class FacultySubjectRequest {

    @NotNull(message = "Faculty is required")
    private Integer facultyId;

    @NotNull(message = "Subject is required")
    private Integer subjectId;

    @NotNull(message = "Class is required")
    private Integer classId;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    public Integer getFacultyId() { return facultyId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
