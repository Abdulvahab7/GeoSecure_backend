package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.CoordinatorAssignRequest;
import com.geosecure.attendance.dto.request.CoordinatorTransferRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.dto.response.CoordinatorHistoryResponse;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.service.CoordinatorService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-side coordinator operations. One class -> one coordinator, one
 * faculty -> many classes. Coordinator-initiated transfer/leave live on
 * FacultyController since only the current coordinator may call them.
 */
@RestController
@RequestMapping("/api/admin/coordinators")
public class AdminCoordinatorController {

    private final CoordinatorService coordinatorService;

    public AdminCoordinatorController(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @PostMapping("/assign")
    public ApiResponse<ClassResponse> assign(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody CoordinatorAssignRequest request) {
        return ApiResponse.ok(coordinatorService.assign(request, principal.getId()), "Coordinator assigned");
    }

    @PostMapping("/force-transfer")
    public ApiResponse<ClassResponse> forceTransfer(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody CoordinatorTransferRequest request) {
        return ApiResponse.ok(coordinatorService.forceTransfer(request, principal.getId()), "Coordinator forcibly transferred");
    }

    @GetMapping("/history/{classId}")
    public ApiResponse<List<CoordinatorHistoryResponse>> history(@PathVariable Integer classId) {
        return ApiResponse.ok(coordinatorService.historyForClass(classId));
    }
}
