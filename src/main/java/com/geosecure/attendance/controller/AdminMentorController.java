package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.MentorAssignRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.service.MentorService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mentor rules: Student -> exactly one mentor; Faculty -> many students.
 * Mentor is a responsibility, not a role - this endpoint is admin-only.
 */
@RestController
@RequestMapping("/api/admin/mentors")
public class AdminMentorController {

    private final MentorService mentorService;

    public AdminMentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    @PostMapping("/assign")
    public ApiResponse<StudentResponse> assign(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody MentorAssignRequest request) {
        return ApiResponse.ok(mentorService.assignMentor(request, principal.getId()), "Mentor assigned");
    }
}
