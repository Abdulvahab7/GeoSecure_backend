package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.FacultySubjectRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.FacultySubjectResponse;
import com.geosecure.attendance.service.FacultySubjectService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/faculty-subjects")
public class AdminFacultySubjectController {

    private final FacultySubjectService facultySubjectService;

    public AdminFacultySubjectController(FacultySubjectService facultySubjectService) {
        this.facultySubjectService = facultySubjectService;
    }

    @PostMapping
    public ApiResponse<FacultySubjectResponse> assign(@Valid @RequestBody FacultySubjectRequest request) {
        return ApiResponse.ok(facultySubjectService.assign(request), "Faculty-subject assignment created");
    }

    @GetMapping("/faculty/{facultyId}")
    public ApiResponse<List<FacultySubjectResponse>> forFaculty(@PathVariable Integer facultyId) {
        return ApiResponse.ok(facultySubjectService.myAssignments(facultyId));
    }
}
