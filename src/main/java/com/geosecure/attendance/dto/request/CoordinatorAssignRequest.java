package com.geosecure.attendance.dto.request;

import jakarta.validation.constraints.NotNull;

/** One class -> one coordinator (a FACULTY-role user; a faculty may coordinate many classes). */
public class CoordinatorAssignRequest {

    @NotNull(message = "Class is required")
    private Integer classId;

    @NotNull(message = "Coordinator faculty is required")
    private Integer facultyId;

    private String reason;

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public Integer getFacultyId() { return facultyId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
