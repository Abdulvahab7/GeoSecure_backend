package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.ClassEntity;

public class ClassResponse {

    private Integer id;
    private String name;
    private Integer departmentId;
    private String departmentName;
    private Integer semester;
    private String section;
    private String academicYear;
    private Integer coordinatorFacultyId;
    private String coordinatorFacultyName;
    private Long studentCount;

    public static ClassResponse from(ClassEntity c) {
        ClassResponse r = new ClassResponse();
        r.id = c.getId();
        r.name = c.getName();
        r.departmentId = c.getDepartment() != null ? c.getDepartment().getId() : null;
        r.departmentName = c.getDepartment() != null ? c.getDepartment().getName() : null;
        r.semester = c.getSemester();
        r.section = c.getSection();
        r.academicYear = c.getAcademicYear();
        if (c.getCoordinatorFaculty() != null) {
            r.coordinatorFacultyId = c.getCoordinatorFaculty().getId();
            r.coordinatorFacultyName = c.getCoordinatorFaculty().getName();
        }
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public Integer getCoordinatorFacultyId() { return coordinatorFacultyId; }
    public void setCoordinatorFacultyId(Integer coordinatorFacultyId) { this.coordinatorFacultyId = coordinatorFacultyId; }
    public String getCoordinatorFacultyName() { return coordinatorFacultyName; }
    public void setCoordinatorFacultyName(String coordinatorFacultyName) { this.coordinatorFacultyName = coordinatorFacultyName; }
    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long studentCount) { this.studentCount = studentCount; }
}
