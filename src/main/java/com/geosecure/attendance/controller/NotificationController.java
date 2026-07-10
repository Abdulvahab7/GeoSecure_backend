package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.entity.Notification;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.service.NotificationService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Available to any authenticated user regardless of role - notifications are per-user, not per-role. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<Notification>> myNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(notificationService.myNotifications(principal.getId()));
    }

    @GetMapping("/unread")
    public ApiResponse<List<Notification>> myUnread(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(notificationService.myUnread(principal.getId()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(Map.of("count", notificationService.unreadCount(principal.getId())));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        notificationService.markRead(id, principal.getId());
        return ApiResponse.ok(null, "Marked as read");
    }
}
