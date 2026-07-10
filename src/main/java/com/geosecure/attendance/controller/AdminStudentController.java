package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.CreateStudentRequest;
import com.geosecure.attendance.dto.request.UpdateStudentRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.service.StudentService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    private final StudentService studentService;

    public AdminStudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<List<StudentResponse>> findAll(@RequestParam(required = false) Integer classId) {
        return ApiResponse.ok(classId != null ? studentService.findByClass(classId) : studentService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(studentService.findById(id));
    }

    @PostMapping
    public ApiResponse<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request) {
        return ApiResponse.ok(studentService.create(request), "Student created");
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentResponse> update(@PathVariable Integer id, @RequestBody UpdateStudentRequest request) {
        return ApiResponse.ok(studentService.update(id, request), "Student updated");
    }
}
