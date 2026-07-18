package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.CoordinatorTransferRequest;
import com.geosecure.attendance.dto.request.GenerateQrRequest;
import com.geosecure.attendance.dto.request.ManualAttendanceRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.AttendanceRecordResponse;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.dto.response.FacultyResponse;
import com.geosecure.attendance.dto.response.FacultySubjectResponse;
import com.geosecure.attendance.dto.response.QrSessionResponse;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.dto.response.TimetableResponse;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.exception.ForbiddenException;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.service.AttendanceService;
import com.geosecure.attendance.service.CoordinatorService;
import com.geosecure.attendance.service.FacultyService;
import com.geosecure.attendance.service.FacultySubjectService;
import com.geosecure.attendance.service.ReportService;
import com.geosecure.attendance.service.TimetableService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.geosecure.attendance.service.UserService;
import com.geosecure.attendance.dto.response.UserResponse;

/**
 * Everything here is scoped to the logged-in faculty member (resolved via
 * UserPrincipal -> Faculty). Coordinator transfer/leave live here because
 * only the CURRENT coordinator of a class may call them - an admin uses
 * AdminCoordinatorController.forceTransfer() instead.
 */
@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;
    private final TimetableService timetableService;
    private final FacultySubjectService facultySubjectService;
    private final AttendanceService attendanceService;
    private final CoordinatorService coordinatorService;
    private final UserService userService;
    private final ReportService reportService;

    public FacultyController(FacultyService facultyService,
                             TimetableService timetableService,
                             FacultySubjectService facultySubjectService,
                             AttendanceService attendanceService,
                             CoordinatorService coordinatorService,
                             UserService userService,
                             ReportService reportService) {
        this.facultyService = facultyService;
        this.timetableService = timetableService;
        this.facultySubjectService = facultySubjectService;
        this.attendanceService = attendanceService;
        this.coordinatorService = coordinatorService;
        this.userService = userService;
        this.reportService = reportService;
    }

    @GetMapping("/profile")
    public ApiResponse<FacultyResponse> profile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(facultyService.findByUserId(principal.getId()));
    }

    @GetMapping("/dashboard/today")
    public ApiResponse<List<TimetableResponse>> todaySchedule(@AuthenticationPrincipal UserPrincipal principal) {
        Faculty faculty = facultyService.requireByUserId(principal.getId());
        return ApiResponse.ok(timetableService.todayForFaculty(faculty.getId()));
    }

    @GetMapping("/timetable")
    public ApiResponse<List<TimetableResponse>> weekTimetable(@AuthenticationPrincipal UserPrincipal principal) {
        Faculty faculty = facultyService.requireByUserId(principal.getId());
        return ApiResponse.ok(timetableService.weekForFaculty(faculty.getId()));
    }

    @GetMapping("/subjects")
    public ApiResponse<List<FacultySubjectResponse>> mySubjects(@AuthenticationPrincipal UserPrincipal principal) {
        Faculty faculty = facultyService.requireByUserId(principal.getId());
        return ApiResponse.ok(facultySubjectService.myAssignments(faculty.getId()));
    }

    @GetMapping("/mentees")
    public ApiResponse<List<StudentResponse>> myMentees(@AuthenticationPrincipal UserPrincipal principal) {
        Faculty faculty = facultyService.requireByUserId(principal.getId());
        return ApiResponse.ok(facultyService.myMentees(faculty.getId()));
    }

    @GetMapping("/coordinated-classes")
    public ApiResponse<List<ClassResponse>> myCoordinatedClasses(@AuthenticationPrincipal UserPrincipal principal) {
        Faculty faculty = facultyService.requireByUserId(principal.getId());
        return ApiResponse.ok(facultyService.myCoordinatedClasses(faculty.getId()));
    }

    // --- Reports ---

    /**
     * Faculty-scoped subject report. Replaces the old frontend call to
     * GET /api/admin/reports/faculty-subjects/{id}, which is gated by
     * hasRole("ADMIN") in SecurityConfig and always 403s for a FACULTY-role
     * JWT. This endpoint also verifies the facultySubjectId actually belongs
     * to the calling faculty member before returning data - the admin
     * endpoint has no such ownership check, so simply relaxing its role
     * requirement would have let any faculty pull any other faculty's report.
     */
    @GetMapping("/reports/subject/{facultySubjectId}")
    public ApiResponse<List<Map<String, Object>>> subjectReport(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @PathVariable Integer facultySubjectId) {
        Faculty faculty = facultyService.requireByUserId(principal.getId());
        boolean owns = facultySubjectService.myAssignments(faculty.getId()).stream()
                .anyMatch(fs -> fs.getId().equals(facultySubjectId));
        if (!owns) {
            throw new ForbiddenException("This subject assignment does not belong to you.");
        }
        return ApiResponse.ok(reportService.subjectReport(facultySubjectId));
    }

    // --- Attendance / QR ---

    @PostMapping("/attendance/generate-qr")
    public ApiResponse<QrSessionResponse> generateQr(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody GenerateQrRequest request) {
        return ApiResponse.ok(attendanceService.generateQr(request, principal.getId()), "QR session started");
    }

    @PostMapping("/attendance/mark-manual")
    public ApiResponse<AttendanceRecordResponse> markManual(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody ManualAttendanceRequest request) {
        return ApiResponse.ok(attendanceService.markManually(request, principal.getId()), "Attendance recorded");
    }

    @GetMapping("/attendance/session/{sessionId}/roster")
    public ApiResponse<List<Map<String, Object>>> roster(@AuthenticationPrincipal UserPrincipal principal,
                                                          @PathVariable Integer sessionId) {
        return ApiResponse.ok(attendanceService.sessionRoster(sessionId, principal.getId()));
    }

    @PostMapping("/attendance/session/{sessionId}/end")
    public ApiResponse<Void> endSession(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer sessionId) {
        attendanceService.endSession(sessionId, principal.getId());
        return ApiResponse.ok(null, "Session ended");
    }

    // --- Coordinator self-service (only valid if this faculty IS the current coordinator) ---

    @PostMapping("/coordinator/transfer")
    public ApiResponse<ClassResponse> transferCoordination(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody CoordinatorTransferRequest request) {
        return ApiResponse.ok(coordinatorService.transfer(request, principal.getId()), "Coordination transferred");
    }

    @PostMapping("/coordinator/leave/{classId}")
    public ApiResponse<ClassResponse> leaveCoordination(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable Integer classId) {
        return ApiResponse.ok(coordinatorService.leave(classId, principal.getId()), "You have left coordination of this class");
    }

    // --- Pending User approvals ---

    @GetMapping("/users/pending")
    public ApiResponse<List<UserResponse>> findPending(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.findPending(principal));
    }

    @PutMapping("/users/{id}/approve")
    public ApiResponse<UserResponse> approve(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.approve(id, principal), "Registration approved");
    }

    @PutMapping("/users/{id}/reject")
    public ApiResponse<UserResponse> reject(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.reject(id, principal), "Registration rejected");
    }
}
