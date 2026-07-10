package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.response.AuditLogResponse;
import com.geosecure.attendance.entity.AuditLog;
import com.geosecure.attendance.entity.User;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.AuditLogRepository;
import com.geosecure.attendance.repository.UserRepository;
import com.geosecure.attendance.service.AuditLogService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void record(Integer userId, String action, String tableName, Integer recordId,
                        String oldValue, String newValue, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> recentGlobal() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> forUser(Integer userId) {
        return auditLogRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
