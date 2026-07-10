package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.ClassCoordinatorHistory;

import java.time.LocalDateTime;

public class CoordinatorHistoryResponse {

    private Integer id;
    private Integer classId;
    private String className;
    private Integer oldFacultyId;
    private String oldFacultyName;
    private Integer newFacultyId;
    private String newFacultyName;
    private Integer transferredById;
    private String transferReason;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;

    public static CoordinatorHistoryResponse from(ClassCoordinatorHistory h) {
        CoordinatorHistoryResponse r = new CoordinatorHistoryResponse();
        r.id = h.getId();
        r.classId = h.getClassEntity().getId();
        r.className = h.getClassEntity().getName();
        if (h.getOldFaculty() != null) {
            r.oldFacultyId = h.getOldFaculty().getId();
            r.oldFacultyName = h.getOldFaculty().getName();
        }
        if (h.getNewFaculty() != null) {
            r.newFacultyId = h.getNewFaculty().getId();
            r.newFacultyName = h.getNewFaculty().getName();
        }
        r.transferredById = h.getTransferredBy().getId();
        r.transferReason = h.getTransferReason();
        r.assignedAt = h.getAssignedAt();
        r.releasedAt = h.getReleasedAt();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getOldFacultyId() { return oldFacultyId; }
    public void setOldFacultyId(Integer oldFacultyId) { this.oldFacultyId = oldFacultyId; }
    public String getOldFacultyName() { return oldFacultyName; }
    public void setOldFacultyName(String oldFacultyName) { this.oldFacultyName = oldFacultyName; }
    public Integer getNewFacultyId() { return newFacultyId; }
    public void setNewFacultyId(Integer newFacultyId) { this.newFacultyId = newFacultyId; }
    public String getNewFacultyName() { return newFacultyName; }
    public void setNewFacultyName(String newFacultyName) { this.newFacultyName = newFacultyName; }
    public Integer getTransferredById() { return transferredById; }
    public void setTransferredById(Integer transferredById) { this.transferredById = transferredById; }
    public String getTransferReason() { return transferReason; }
    public void setTransferReason(String transferReason) { this.transferReason = transferReason; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
}
