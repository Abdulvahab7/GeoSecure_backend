package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.TimetableRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.TimetableResponse;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.service.TimetableService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/timetable")
public class AdminTimetableController {

    private final TimetableService timetableService;

    public AdminTimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping
    public ApiResponse<List<TimetableResponse>> findForClass(@RequestParam(required = false) Integer classId) {
        return ApiResponse.ok(timetableService.findForClass(classId));
    }

    @PostMapping
    public ApiResponse<TimetableResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                  @Valid @RequestBody TimetableRequest request) {
        return ApiResponse.ok(timetableService.create(request, principal.getId()), "Timetable slot created");
    }
}
