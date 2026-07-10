package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.CreateStudentRequest;
import com.geosecure.attendance.dto.request.UpdateStudentRequest;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.entity.ClassEntity;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.entity.Role;
import com.geosecure.attendance.entity.Student;
import com.geosecure.attendance.entity.User;
import com.geosecure.attendance.exception.BadRequestException;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.ClassRepository;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.repository.RoleRepository;
import com.geosecure.attendance.repository.StudentRepository;
import com.geosecure.attendance.repository.UserRepository;
import com.geosecure.attendance.service.StudentService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClassRepository classRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               ClassRepository classRepository,
                               FacultyRepository facultyRepository,
                               PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.classRepository = classRepository;
        this.facultyRepository = facultyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream().map(StudentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findByClass(Integer classId) {
        return studentRepository.findByClassEntity_Id(classId).stream().map(StudentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findById(Integer id) {
        return StudentResponse.from(requireById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findByUserId(Integer userId) {
        return StudentResponse.from(requireByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Student requireByUserId(Integer userId) {
        return studentRepository.findByUserIdWithClassAndDepartment(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for this user."));
    }

    @Override
    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (studentRepository.existsByRegisterNo(request.getRegisterNo())) {
            throw new DuplicateResourceException("Register number '" + request.getRegisterNo() + "' is already in use.");
        }

        Role studentRole = roleRepository.findByName(Role.RoleName.student)
                .orElseThrow(() -> new ResourceNotFoundException("Student role not seeded."));
        ClassEntity classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + request.getClassId()));
        Faculty mentor = facultyRepository.findById(request.getMentorFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor faculty not found: " + request.getMentorFacultyId()));

        if (!Boolean.TRUE.equals(mentor.getIsMentor())) {
            throw new BadRequestException("Faculty '" + mentor.getName() + "' is not flagged as mentor-eligible (is_mentor = false).");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(studentRole);
        user.setIsActive(true);
        user = userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setRegisterNo(request.getRegisterNo());
        student.setName(request.getName());
        student.setClassEntity(classEntity);
        student.setMentorFaculty(mentor);
        student.setDateOfBirth(request.getDateOfBirth());
        student.setPhone(request.getPhone());

        return StudentResponse.from(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentResponse update(Integer id, UpdateStudentRequest request) {
        Student student = requireById(id);

        if (request.getName() != null) student.setName(request.getName());
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());
        if (request.getPhone() != null) student.setPhone(request.getPhone());
        if (request.getClassId() != null) {
            ClassEntity classEntity = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + request.getClassId()));
            student.setClassEntity(classEntity);
        }

        return StudentResponse.from(studentRepository.save(student));
    }

    private Student requireById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }
}
