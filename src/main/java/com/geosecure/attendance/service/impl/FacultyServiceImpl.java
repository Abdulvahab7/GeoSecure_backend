package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.CreateFacultyRequest;
import com.geosecure.attendance.dto.request.UpdateFacultyRequest;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.dto.response.FacultyResponse;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.entity.Department;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.entity.Role;
import com.geosecure.attendance.entity.User;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.ClassRepository;
import com.geosecure.attendance.repository.DepartmentRepository;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.repository.RoleRepository;
import com.geosecure.attendance.repository.StudentRepository;
import com.geosecure.attendance.repository.UserRepository;
import com.geosecure.attendance.service.FacultyService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyServiceImpl(FacultyRepository facultyRepository,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               DepartmentRepository departmentRepository,
                               StudentRepository studentRepository,
                               ClassRepository classRepository,
                               PasswordEncoder passwordEncoder) {
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyResponse> findAll() {
        return facultyRepository.findAllWithDepartment().stream().map(FacultyResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse findById(Integer id) {
        return FacultyResponse.from(requireById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse findByUserId(Integer userId) {
        return FacultyResponse.from(requireByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Faculty requireByUserId(Integer userId) {
        return facultyRepository.findByUserIdWithDepartment(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for this user."));
    }

    @Override
    @Transactional
    public FacultyResponse create(CreateFacultyRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (facultyRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID '" + request.getEmployeeId() + "' is already in use.");
        }

        Role facultyRole = roleRepository.findByName(Role.RoleName.faculty)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty role not seeded."));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(facultyRole);
        user.setIsActive(true);
        user = userRepository.save(user);

        Faculty faculty = new Faculty();
        faculty.setUser(user);
        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setName(request.getName());
        faculty.setDepartment(department);
        faculty.setDesignation(request.getDesignation());
        faculty.setPhone(request.getPhone());
        faculty.setIsMentor(Boolean.TRUE.equals(request.getIsMentor()));

        return FacultyResponse.from(facultyRepository.save(faculty));
    }

    @Override
    @Transactional
    public FacultyResponse update(Integer id, UpdateFacultyRequest request) {
        Faculty faculty = requireById(id);

        if (request.getName() != null) faculty.setName(request.getName());
        if (request.getDesignation() != null) faculty.setDesignation(request.getDesignation());
        if (request.getPhone() != null) faculty.setPhone(request.getPhone());
        if (request.getIsMentor() != null) faculty.setIsMentor(request.getIsMentor());
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            faculty.setDepartment(department);
        }

        return FacultyResponse.from(facultyRepository.save(faculty));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyResponse> findMentorEligible() {
        return facultyRepository.findAllWithDepartment().stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsMentor()))
                .map(FacultyResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> myMentees(Integer facultyId) {
        return studentRepository.findByMentorFacultyIdWithClassAndDepartment(facultyId).stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> myCoordinatedClasses(Integer facultyId) {
        return classRepository.findCoordinatedByFacultyId(facultyId).stream()
                .map(ClassResponse::from)
                .toList();
    }

    private Faculty requireById(Integer id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + id));
    }
}
