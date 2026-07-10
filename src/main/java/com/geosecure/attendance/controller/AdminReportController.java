package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.service.ReportService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Admin-facing reporting endpoints - wires the native @Query aggregates from AttendanceRecordRepository/AttendanceSessionRepository. */
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/faculty-subjects/{facultySubjectId}")
    public ApiResponse<List<Map<String, Object>>> subjectReport(@PathVariable Integer facultySubjectId) {
        return ApiResponse.ok(reportService.subjectReport(facultySubjectId));
    }

    @GetMapping("/defaulters/{classId}")
    public ApiResponse<List<Map<String, Object>>> defaulters(@PathVariable Integer classId,
                                                              @RequestParam(required = false) Double threshold) {
        return ApiResponse.ok(reportService.defaulterList(classId, threshold));
    }

    @GetMapping("/class-matrix/{classId}")
    public ApiResponse<List<Map<String, Object>>> classMatrix(
            @PathVariable Integer classId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(reportService.classMatrix(classId, subjectId, startDate, endDate));
    }

    @GetMapping("/department-averages")
    public ApiResponse<List<Map<String, Object>>> departmentAverages() {
        return ApiResponse.ok(reportService.departmentAverages());
    }

    @GetMapping("/today-stats")
    public ApiResponse<Map<String, Object>> todayStats() {
        return ApiResponse.ok(reportService.todayStats());
    }

    @GetMapping("/daily-stats")
    public ApiResponse<List<Map<String, Object>>> dailyStats(@RequestParam(defaultValue = "7") int sinceDays) {
        return ApiResponse.ok(reportService.dailySessionStats(sinceDays));
    }

    /** Phase 3: monthly attendance report for a class, optionally filtered to one year/month. */
    @GetMapping("/monthly/{classId}")
    public ApiResponse<List<Map<String, Object>>> monthlyReport(@PathVariable Integer classId,
                                                                 @RequestParam(required = false) Integer year,
                                                                 @RequestParam(required = false) Integer month) {
        return ApiResponse.ok(reportService.monthlyClassReport(classId, year, month));
    }

    @GetMapping("/daily/{classId}")
    public ApiResponse<List<Map<String, Object>>> dailyReport(@PathVariable Integer classId,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(reportService.dailyClassReport(classId, date));
    }

    @GetMapping("/semester/{classId}")
    public ApiResponse<List<Map<String, Object>>> semesterReport(@PathVariable Integer classId,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(reportService.semesterReport(classId, from, to));
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<List<Map<String, Object>>> studentReport(@PathVariable Integer studentId) {
        return ApiResponse.ok(reportService.studentSubjectSummary(studentId));
    }

    @GetMapping("/faculty/{facultyId}")
    public ApiResponse<List<Map<String, Object>>> facultyReport(@PathVariable Integer facultyId) {
        return ApiResponse.ok(reportService.facultyReport(facultyId));
    }
}

