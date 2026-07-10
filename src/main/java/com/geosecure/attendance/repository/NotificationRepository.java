package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * No direct Node.js equivalent existed for this table in the original
 * controllers (it was schema-only / reserved for future use), but is
 * included here for completeness since the table exists in schema.sql.
 */
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Integer userId);

    List<Notification> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);

    long countByUser_IdAndIsReadFalse(Integer userId);
}
