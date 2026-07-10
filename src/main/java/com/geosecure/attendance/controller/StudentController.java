package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.ScanQrRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.AttendanceRecordResponse;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.entity.Student;
import com.geosecure.attendance.security.UserPrincipal;
import com.geosecure.attendance.service.AttendanceService;
import com.geosecure.attendance.service.ReportService;
import com.geosecure.attendance.service.StudentService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Everything here is scoped to the logged-in student (resolved via UserPrincipal -> Student). */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ReportService reportService;

    public StudentController(StudentService studentService, AttendanceService attendanceService, ReportService reportService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.reportService = reportService;
    }

    @GetMapping("/profile")
    public ApiResponse<StudentResponse> profile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(studentService.findByUserId(principal.getId()));
    }

    @PostMapping("/attendance/scan-qr")
    public ApiResponse<AttendanceRecordResponse> scanQr(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody ScanQrRequest request) {
        return ApiResponse.ok(attendanceService.scanQr(request, principal.getId()), "Attendance marked present");
    }

    @GetMapping("/attendance/summary")
    public ApiResponse<List<Map<String, Object>>> attendanceSummary(@AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.requireByUserId(principal.getId());
        return ApiResponse.ok(reportService.studentSubjectSummary(student.getId()));
    }

    @GetMapping("/attendance/recent")
    public ApiResponse<List<Map<String, Object>>> recentActivity(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @RequestParam(defaultValue = "10") int limit) {
        Student student = studentService.requireByUserId(principal.getId());
        return ApiResponse.ok(reportService.studentRecentActivity(student.getId(), limit));
    }

    @GetMapping("/attendance/subject/{subjectId}")
    public ApiResponse<List<Map<String, Object>>> subjectDetail(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable Integer subjectId) {
        Student student = studentService.requireByUserId(principal.getId());
        return ApiResponse.ok(reportService.studentSubjectDetail(student.getId(), subjectId));
    }

    /** Phase 3: the student's own month-wise attendance history (all subjects combined). */
    @GetMapping("/attendance/monthly")
    public ApiResponse<List<Map<String, Object>>> monthlyHistory(@AuthenticationPrincipal UserPrincipal principal) {
        Student student = studentService.requireByUserId(principal.getId());
        return ApiResponse.ok(reportService.studentMonthlyReport(student.getId()));
    }
}
