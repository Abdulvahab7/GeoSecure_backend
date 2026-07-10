package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.ClassRequest;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.entity.ClassEntity;
import com.geosecure.attendance.entity.Department;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.ClassRepository;
import com.geosecure.attendance.repository.DepartmentRepository;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.service.ClassService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;

    public ClassServiceImpl(ClassRepository classRepository, DepartmentRepository departmentRepository, FacultyRepository facultyRepository) {
        this.classRepository = classRepository;
        this.departmentRepository = departmentRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> findAll() {
        return classRepository.findAllWithDepartmentOrdered().stream()
                .map(c -> withCount(ClassResponse.from(c), c.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse findById(Integer id) {
        return withCount(ClassResponse.from(requireById(id)), id);
    }

    @Override
    @Transactional
    public ClassResponse create(ClassRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(request.getName());
        classEntity.setDepartment(department);
        classEntity.setSemester(request.getSemester());
        classEntity.setSection(request.getSection());
        classEntity.setAcademicYear(request.getAcademicYear());

        if (request.getCoordinatorFacultyId() != null) {
            Faculty coordinator = facultyRepository.findById(request.getCoordinatorFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + request.getCoordinatorFacultyId()));
            classEntity.setCoordinatorFaculty(coordinator);
        }

        return ClassResponse.from(classRepository.save(classEntity));
    }

    @Override
    @Transactional
    public ClassResponse update(Integer id, ClassRequest request) {
        ClassEntity classEntity = requireById(id);
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

        classEntity.setName(request.getName());
        classEntity.setDepartment(department);
        classEntity.setSemester(request.getSemester());
        classEntity.setSection(request.getSection());
        classEntity.setAcademicYear(request.getAcademicYear());

        return withCount(ClassResponse.from(classRepository.save(classEntity)), id);
    }

    private ClassResponse withCount(ClassResponse response, Integer classId) {
        response.setStudentCount(classRepository.countStudentsInClass(classId));
        return response;
    }

    private ClassEntity requireById(Integer id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
    }
}
