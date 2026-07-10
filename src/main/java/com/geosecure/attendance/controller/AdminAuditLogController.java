package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.AuditLogResponse;
import com.geosecure.attendance.service.AuditLogService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    public AdminAuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> recent() {
        return ApiResponse.ok(auditLogService.recentGlobal());
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<AuditLogResponse>> forUser(@PathVariable Integer userId) {
        return ApiResponse.ok(auditLogService.forUser(userId));
    }
}
