package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.Subject;

public class SubjectResponse {

    private Integer id;
    private String code;
    private String name;
    private Integer departmentId;
    private String departmentName;
    private Integer semester;
    private Integer credits;
    private String subjectType;

    public static SubjectResponse from(Subject s) {
        SubjectResponse r = new SubjectResponse();
        r.id = s.getId();
        r.code = s.getCode();
        r.name = s.getName();
        r.departmentId = s.getDepartment() != null ? s.getDepartment().getId() : null;
        r.departmentName = s.getDepartment() != null ? s.getDepartment().getName() : null;
        r.semester = s.getSemester();
        r.credits = s.getCredits();
        r.subjectType = s.getSubjectType() != null ? s.getSubjectType().name() : null;
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
}
