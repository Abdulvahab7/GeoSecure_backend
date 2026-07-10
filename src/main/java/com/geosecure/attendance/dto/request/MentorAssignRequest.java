package com.geosecure.attendance.dto.request;

import jakarta.validation.constraints.NotNull;

/** One student -> one mentor (a FACULTY-role user with faculty.is_mentor = true). */
public class MentorAssignRequest {

    @NotNull(message = "Student is required")
    private Integer studentId;

    @NotNull(message = "Mentor faculty is required")
    private Integer mentorFacultyId;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getMentorFacultyId() { return mentorFacultyId; }
    public void setMentorFacultyId(Integer mentorFacultyId) { this.mentorFacultyId = mentorFacultyId; }
}
