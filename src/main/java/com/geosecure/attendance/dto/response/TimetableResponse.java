package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.Timetable;

public class TimetableResponse {

    private Integer id;
    private Integer classId;
    private String className;
    private Integer facultyId;
    private String facultyName;
    private Integer subjectId;
    private String subjectName;
    private Integer facultySubjectId;
    private String dayOfWeek;
    private Integer sessionNumber;
    private String roomNumber;

    public static TimetableResponse from(Timetable t) {
        TimetableResponse r = new TimetableResponse();
        r.id = t.getId();
        r.classId = t.getClassEntity().getId();
        r.className = t.getClassEntity().getName();
        r.facultyId = t.getFaculty().getId();
        r.facultyName = t.getFaculty().getName();
        r.subjectId = t.getSubject().getId();
        r.subjectName = t.getSubject().getName();
        r.facultySubjectId = t.getFacultySubject() != null ? t.getFacultySubject().getId() : null;
        r.dayOfWeek = t.getDayOfWeek().name();
        r.sessionNumber = t.getSessionNumber();
        r.roomNumber = t.getRoomNumber();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getFacultyId() { return facultyId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Integer getFacultySubjectId() { return facultySubjectId; }
    public void setFacultySubjectId(Integer facultySubjectId) { this.facultySubjectId = facultySubjectId; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(Integer sessionNumber) { this.sessionNumber = sessionNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
