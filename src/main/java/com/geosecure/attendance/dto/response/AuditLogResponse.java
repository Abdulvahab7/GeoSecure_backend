package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Integer id;
    private Integer userId;
    private String username;
    private String action;
    private String tableName;
    private Integer recordId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog a) {
        AuditLogResponse r = new AuditLogResponse();
        r.id = a.getId();
        r.userId = a.getUser().getId();
        r.username = a.getUser().getUsername();
        r.action = a.getAction();
        r.tableName = a.getTableName();
        r.recordId = a.getRecordId();
        r.oldValue = a.getOldValue();
        r.newValue = a.getNewValue();
        r.ipAddress = a.getIpAddress();
        r.createdAt = a.getCreatedAt();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
