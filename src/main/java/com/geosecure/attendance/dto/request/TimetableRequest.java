package com.geosecure.attendance.dto.request;

import com.geosecure.attendance.entity.Timetable;

import jakarta.validation.constraints.NotNull;

public class TimetableRequest {

    @NotNull(message = "Class is required")
    private Integer classId;

    @NotNull(message = "Faculty is required")
    private Integer facultyId;

    @NotNull(message = "Subject is required")
    private Integer subjectId;

    /**
     * Deprecated/ignored: the server always resolves (and creates, if missing) the correct
     * faculty_subjects row from facultyId + subjectId + classId. A client-supplied value here
     * is never used - that was the source of the faculty_subject_id-ends-up-NULL bug.
     */
    private Integer facultySubjectId;

    @NotNull(message = "Day of week is required")
    private Timetable.DayOfWeek dayOfWeek;

    @NotNull(message = "Session number is required")
    private Integer sessionNumber;

    private String roomNumber;

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public Integer getFacultyId() { return facultyId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public Integer getFacultySubjectId() { return facultySubjectId; }
    public void setFacultySubjectId(Integer facultySubjectId) { this.facultySubjectId = facultySubjectId; }
    public Timetable.DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Timetable.DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(Integer sessionNumber) { this.sessionNumber = sessionNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
