package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.SubjectRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.SubjectResponse;
import com.geosecure.attendance.service.SubjectService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {

    private final SubjectService subjectService;

    public AdminSubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ApiResponse<List<SubjectResponse>> findAll() {
        return ApiResponse.ok(subjectService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SubjectResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(subjectService.findById(id));
    }

    @PostMapping
    public ApiResponse<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return ApiResponse.ok(subjectService.create(request), "Subject created");
    }

    @PutMapping("/{id}")
    public ApiResponse<SubjectResponse> update(@PathVariable Integer id, @Valid @RequestBody SubjectRequest request) {
        return ApiResponse.ok(subjectService.update(id, request), "Subject updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        subjectService.delete(id);
        return ApiResponse.ok(null, "Subject deleted");
    }
}
