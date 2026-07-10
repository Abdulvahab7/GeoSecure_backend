package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.SubjectRequest;
import com.geosecure.attendance.dto.response.SubjectResponse;
import com.geosecure.attendance.entity.Department;
import com.geosecure.attendance.entity.Subject;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.DepartmentRepository;
import com.geosecure.attendance.repository.SubjectRepository;
import com.geosecure.attendance.service.SubjectService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository, DepartmentRepository departmentRepository) {
        this.subjectRepository = subjectRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll() {
        return subjectRepository.findAllWithDepartmentOrderByCode().stream().map(SubjectResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse findById(Integer id) {
        return SubjectResponse.from(requireById(id));
    }

    @Override
    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("A subject with code '" + request.getCode() + "' already exists.");
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

        Subject subject = new Subject();
        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setDepartment(department);
        subject.setSemester(request.getSemester());
        subject.setCredits(request.getCredits());
        subject.setSubjectType(request.getSubjectType());

        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public SubjectResponse update(Integer id, SubjectRequest request) {
        Subject subject = requireById(id);
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setDepartment(department);
        subject.setSemester(request.getSemester());
        subject.setCredits(request.getCredits());
        subject.setSubjectType(request.getSubjectType());

        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        subjectRepository.delete(requireById(id));
    }

    private Subject requireById(Integer id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + id));
    }
}
