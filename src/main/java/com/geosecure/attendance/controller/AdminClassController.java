package com.geosecure.attendance.controller;

import com.geosecure.attendance.dto.request.ClassRequest;
import com.geosecure.attendance.dto.response.ApiResponse;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.service.ClassService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/classes")
public class AdminClassController {

    private final ClassService classService;

    public AdminClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public ApiResponse<List<ClassResponse>> findAll() {
        return ApiResponse.ok(classService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ClassResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(classService.findById(id));
    }

    @PostMapping
    public ApiResponse<ClassResponse> create(@Valid @RequestBody ClassRequest request) {
        return ApiResponse.ok(classService.create(request), "Class created");
    }

    @PutMapping("/{id}")
    public ApiResponse<ClassResponse> update(@PathVariable Integer id, @Valid @RequestBody ClassRequest request) {
        return ApiResponse.ok(classService.update(id, request), "Class updated");
    }
}
