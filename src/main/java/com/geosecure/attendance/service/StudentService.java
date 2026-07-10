package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.CreateStudentRequest;
import com.geosecure.attendance.dto.request.UpdateStudentRequest;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.entity.Student;

import java.util.List;

public interface StudentService {

    List<StudentResponse> findAll();

    List<StudentResponse> findByClass(Integer classId);

    StudentResponse findById(Integer id);

    StudentResponse findByUserId(Integer userId);

    Student requireByUserId(Integer userId);

    StudentResponse create(CreateStudentRequest request);

    StudentResponse update(Integer id, UpdateStudentRequest request);
}
