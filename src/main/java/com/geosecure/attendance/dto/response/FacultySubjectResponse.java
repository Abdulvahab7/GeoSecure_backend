package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.FacultySubject;

public class FacultySubjectResponse {

    private Integer id;
    private Integer facultyId;
    private String facultyName;
    private Integer subjectId;
    private String subjectCode;
    private String subjectName;
    private Integer classId;
    private String className;
    private String academicYear;

    public static FacultySubjectResponse from(FacultySubject fs) {
        FacultySubjectResponse r = new FacultySubjectResponse();
        r.id = fs.getId();
        r.facultyId = fs.getFaculty().getId();
        r.facultyName = fs.getFaculty().getName();
        r.subjectId = fs.getSubject().getId();
        r.subjectCode = fs.getSubject().getCode();
        r.subjectName = fs.getSubject().getName();
        r.classId = fs.getClassEntity().getId();
        r.className = fs.getClassEntity().getName();
        r.academicYear = fs.getAcademicYear();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getFacultyId() { return facultyId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
