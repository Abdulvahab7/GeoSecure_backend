package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.Student;

import java.time.LocalDate;

public class StudentResponse {

    private Integer id;
    private Integer userId;
    private String registerNo;
    private String name;
    private String email;
    private Integer classId;
    private String className;
    private Integer mentorFacultyId;
    private String mentorFacultyName;
    private LocalDate dateOfBirth;
    private String phone;
    private String photoUrl;

    public static StudentResponse from(Student s) {
        StudentResponse r = new StudentResponse();
        r.id = s.getId();
        r.userId = s.getUser() != null ? s.getUser().getId() : null;
        r.registerNo = s.getRegisterNo();
        r.name = s.getName();
        r.email = s.getUser() != null ? s.getUser().getEmail() : null;
        r.classId = s.getClassEntity() != null ? s.getClassEntity().getId() : null;
        r.className = s.getClassEntity() != null ? s.getClassEntity().getName() : null;
        r.mentorFacultyId = s.getMentorFaculty() != null ? s.getMentorFaculty().getId() : null;
        r.mentorFacultyName = s.getMentorFaculty() != null ? s.getMentorFaculty().getName() : null;
        r.dateOfBirth = s.getDateOfBirth();
        r.phone = s.getPhone();
        r.photoUrl = s.getPhotoUrl();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getRegisterNo() { return registerNo; }
    public void setRegisterNo(String registerNo) { this.registerNo = registerNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getMentorFacultyId() { return mentorFacultyId; }
    public void setMentorFacultyId(Integer mentorFacultyId) { this.mentorFacultyId = mentorFacultyId; }
    public String getMentorFacultyName() { return mentorFacultyName; }
    public void setMentorFacultyName(String mentorFacultyName) { this.mentorFacultyName = mentorFacultyName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
