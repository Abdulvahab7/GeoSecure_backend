package com.geosecure.attendance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    List<Map<String, Object>> studentSubjectSummary(Integer studentId);

    List<Map<String, Object>> studentRecentActivity(Integer studentId, int limit);

    List<Map<String, Object>> studentSubjectDetail(Integer studentId, Integer subjectId);

    List<Map<String, Object>> subjectReport(Integer facultySubjectId);

    List<Map<String, Object>> defaulterList(Integer classId, Double thresholdOverride);

    List<Map<String, Object>> classMatrix(Integer classId, Integer subjectId, LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> departmentAverages();

    Map<String, Object> todayStats();

    List<Map<String, Object>> dailySessionStats(int sinceDays);

    /** Phase 3: month-wise attendance % per student for a class (optionally filtered to one year/month). */
    List<Map<String, Object>> monthlyClassReport(Integer classId, Integer year, Integer month);

    /** Phase 3: one student's own month-wise attendance history, across all subjects. */
    List<Map<String, Object>> studentMonthlyReport(Integer studentId);

    List<Map<String, Object>> dailyClassReport(Integer classId, LocalDate date);

    List<Map<String, Object>> semesterReport(Integer classId, LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> facultyReport(Integer facultyId);
}

