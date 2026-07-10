package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.CreateFacultyRequest;
import com.geosecure.attendance.dto.request.UpdateFacultyRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.FacultyResponse;
import com.geosecure.attendance.service.FacultyService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/faculty")
public class AdminFacultyController {

    private final FacultyService facultyService;

    public AdminFacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ApiResponse<List<FacultyResponse>> findAll() {
        return ApiResponse.ok(facultyService.findAll());
    }

    @GetMapping("/mentor-eligible")
    public ApiResponse<List<FacultyResponse>> findMentorEligible() {
        return ApiResponse.ok(facultyService.findMentorEligible());
    }

    @GetMapping("/{id}")
    public ApiResponse<FacultyResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(facultyService.findById(id));
    }

    @PostMapping
    public ApiResponse<FacultyResponse> create(@Valid @RequestBody CreateFacultyRequest request) {
        return ApiResponse.ok(facultyService.create(request), "Faculty created");
    }

    @PutMapping("/{id}")
    public ApiResponse<FacultyResponse> update(@PathVariable Integer id, @RequestBody UpdateFacultyRequest request) {
        return ApiResponse.ok(facultyService.update(id, request), "Faculty updated");
    }
}
