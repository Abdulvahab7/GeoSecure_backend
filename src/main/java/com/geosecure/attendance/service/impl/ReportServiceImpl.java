package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.repository.AttendanceRecordRepository;
import com.geosecure.attendance.repository.AttendanceSessionRepository;
import com.geosecure.attendance.service.ReportService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;

    @Value("${geosecure.attendance.defaulter-threshold}")
    private double defaultDefaulterThreshold;

    public ReportServiceImpl(AttendanceRecordRepository recordRepository, AttendanceSessionRepository sessionRepository) {
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> studentSubjectSummary(Integer studentId) {
        return recordRepository.studentSubjectSummary(studentId).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("subjectId", row[0]);
            m.put("subjectCode", row[1]);
            m.put("subjectName", row[2]);
            m.put("subjectType", row[3]);
            m.put("totalSessions", row[4]);
            m.put("presentCount", row[5]);
            m.put("percentage", row[6]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> studentRecentActivity(Integer studentId, int limit) {
        return recordRepository.studentRecentActivity(studentId, limit).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", row[0]);
            m.put("status", row[1]);
            m.put("scannedAt", row[2]);
            m.put("distanceMeters", row[3]);
            m.put("subjectCode", row[4]);
            m.put("subjectName", row[5]);
            m.put("sessionDate", row[6]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> studentSubjectDetail(Integer studentId, Integer subjectId) {
        return recordRepository.studentSubjectDetail(studentId, subjectId).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sessionDate", row[0]);
            m.put("startedAt", row[1]);
            m.put("status", row[2]);
            m.put("distanceMeters", row[3]);
            m.put("scannedAt", row[4]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> subjectReport(Integer facultySubjectId) {
        return recordRepository.subjectReportStudents(facultySubjectId).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", row[0]);
            m.put("name", row[1]);
            m.put("registerNo", row[2]);
            m.put("present", row[3]);
            m.put("total", row[4]);
            m.put("percentage", row[5]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> defaulterList(Integer classId, Double thresholdOverride) {
        double threshold = thresholdOverride != null ? thresholdOverride : defaultDefaulterThreshold;
        return recordRepository.defaulterList(classId, threshold).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", row[0]);
            m.put("name", row[1]);
            m.put("registerNo", row[2]);
            m.put("subjectCode", row[3]);
            m.put("subjectName", row[4]);
            m.put("totalSessions", row[5]);
            m.put("presentCount", row[6]);
            m.put("percentage", row[7]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> classMatrix(Integer classId, Integer subjectId, LocalDate startDate, LocalDate endDate) {
        return recordRepository.classReportMatrix(classId, subjectId, startDate, endDate).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", row[0]);
            m.put("name", row[1]);
            m.put("registerNo", row[2]);
            m.put("subjectCode", row[3]);
            m.put("subjectName", row[4]);
            m.put("totalSessions", row[5]);
            m.put("presentCount", row[6]);
            m.put("percentage", row[7]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> departmentAverages() {
        return recordRepository.departmentWiseAverageAttendance().stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("department", row[0]);
            m.put("avgAttendance", row[1]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> todayStats() {
        List<Object[]> rows = recordRepository.todayGlobalStats();
        Map<String, Object> m = new LinkedHashMap<>();
        if (!rows.isEmpty()) {
            Object[] row = rows.get(0);
            m.put("sessions", row[0]);
            m.put("totalPresent", row[1]);
            m.put("uniqueStudents", row[2]);
        }
        m.put("totalSessionsAllTime", sessionRepository.count());
        return m;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> dailySessionStats(int sinceDays) {
        LocalDate since = LocalDate.now().minusDays(sinceDays);
        return sessionRepository.findDailySessionStatsSince(since).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sessionDate", row[0]);
            m.put("sessions", row[1]);
            m.put("present", row[2]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> monthlyClassReport(Integer classId, Integer year, Integer month) {
        return recordRepository.monthlyClassReport(classId, year, month).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", row[0]);
            m.put("name", row[1]);
            m.put("registerNo", row[2]);
            m.put("month", row[3]);
            m.put("totalSessions", row[4]);
            m.put("presentCount", row[5]);
            m.put("percentage", row[6]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> studentMonthlyReport(Integer studentId) {
        return recordRepository.studentMonthlyReport(studentId).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", row[0]);
            m.put("totalSessions", row[1]);
            m.put("presentCount", row[2]);
            m.put("percentage", row[3]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> dailyClassReport(Integer classId, LocalDate date) {
        return recordRepository.dailyClassReport(classId, date).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", row[0]);
            m.put("name", row[1]);
            m.put("registerNo", row[2]);
            m.put("subjectCode", row[3]);
            m.put("subjectName", row[4]);
            m.put("status", row[5]);
            m.put("scannedAt", row[6]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> semesterReport(Integer classId, LocalDate startDate, LocalDate endDate) {
        return recordRepository.semesterReport(classId, startDate, endDate).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", row[0]);
            m.put("name", row[1]);
            m.put("registerNo", row[2]);
            m.put("subjectCode", row[3]);
            m.put("subjectName", row[4]);
            m.put("totalSessions", row[5]);
            m.put("presentCount", row[6]);
            m.put("percentage", row[7]);
            return m;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> facultyReport(Integer facultyId) {
        return recordRepository.facultyReport(facultyId).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("assignmentId", row[0]);
            m.put("subjectCode", row[1]);
            m.put("subjectName", row[2]);
            m.put("className", row[3]);
            m.put("classSection", row[4]);
            m.put("totalSessions", row[5]);
            m.put("presentCount", row[6]);
            m.put("totalEligibleSlots", row[7]);
            m.put("percentage", row[8]);
            return m;
        }).toList();
    }
}

