package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.DepartmentRequest;
import com.geosecure.attendance.dto.response.DepartmentResponse;
import com.geosecure.attendance.entity.Department;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.DepartmentRepository;
import com.geosecure.attendance.service.DepartmentService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse findById(Integer id) {
        return DepartmentResponse.from(requireById(id));
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("A department with code '" + request.getCode() + "' already exists.");
        }
        Department d = new Department();
        d.setName(request.getName());
        d.setCode(request.getCode());
        return DepartmentResponse.from(departmentRepository.save(d));
    }

    @Override
    @Transactional
    public DepartmentResponse update(Integer id, DepartmentRequest request) {
        Department d = requireById(id);
        d.setName(request.getName());
        d.setCode(request.getCode());
        return DepartmentResponse.from(departmentRepository.save(d));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Department d = requireById(id);
        departmentRepository.delete(d);
    }

    private Department requireById(Integer id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }
}
