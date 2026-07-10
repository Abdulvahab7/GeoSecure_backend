package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {

    void record(Integer userId, String action, String tableName, Integer recordId,
                String oldValue, String newValue, String ipAddress, String userAgent);

    List<AuditLogResponse> recentGlobal();

    List<AuditLogResponse> forUser(Integer userId);
}
