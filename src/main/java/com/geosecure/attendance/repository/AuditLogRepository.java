package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persists security-sensitive events. Written to from AdminService and
 * AuthService (e.g. login, user creation, password reset, role changes)
 * to provide the forensic audit trail described in the project report.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    List<AuditLog> findByUser_IdOrderByCreatedAtDesc(Integer userId);

    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
