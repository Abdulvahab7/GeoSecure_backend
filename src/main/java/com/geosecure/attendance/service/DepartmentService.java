package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.DepartmentRequest;
import com.geosecure.attendance.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> findAll();

    DepartmentResponse findById(Integer id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Integer id, DepartmentRequest request);

    void delete(Integer id);
}
