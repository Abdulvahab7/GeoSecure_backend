package com.geosecure.attendance.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Used both for a coordinator voluntarily handing off ("transfer ownership")
 * and for an admin forcing a transfer. newFacultyId may be null when a
 * coordinator simply "leaves coordination" with nobody assigned yet.
 */
public class CoordinatorTransferRequest {

    @NotNull(message = "Class is required")
    private Integer classId;

    private Integer newFacultyId;

    private String reason;

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public Integer getNewFacultyId() { return newFacultyId; }
    public void setNewFacultyId(Integer newFacultyId) { this.newFacultyId = newFacultyId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
