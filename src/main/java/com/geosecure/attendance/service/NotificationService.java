package com.geosecure.attendance.service;

import com.geosecure.attendance.entity.Notification;

import java.util.List;

public interface NotificationService {

    void notify(Integer userId, String title, String message, Notification.NotificationType type);

    List<Notification> myNotifications(Integer userId);

    List<Notification> myUnread(Integer userId);

    long unreadCount(Integer userId);

    void markRead(Integer notificationId, Integer userId);
}
