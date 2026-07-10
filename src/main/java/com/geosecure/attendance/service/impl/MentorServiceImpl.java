package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.MentorAssignRequest;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.entity.Notification;
import com.geosecure.attendance.entity.Student;
import com.geosecure.attendance.exception.BadRequestException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.repository.StudentRepository;
import com.geosecure.attendance.service.AuditLogService;
import com.geosecure.attendance.service.MentorService;
import com.geosecure.attendance.service.NotificationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enforces: Student -> exactly one mentor; Faculty -> many students.
 * Mentor is a responsibility a FACULTY-role user carries (faculty.is_mentor),
 * never a separate role.
 */
@Service
public class MentorServiceImpl implements MentorService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public MentorServiceImpl(StudentRepository studentRepository,
                              FacultyRepository facultyRepository,
                              AuditLogService auditLogService,
                              NotificationService notificationService) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public StudentResponse assignMentor(MentorAssignRequest request, Integer actingUserId) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));
        Faculty mentor = facultyRepository.findById(request.getMentorFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + request.getMentorFacultyId()));

        if (!Boolean.TRUE.equals(mentor.getIsMentor())) {
            throw new BadRequestException(
                    "Faculty '" + mentor.getName() + "' is not eligible to mentor (faculty.is_mentor = false). "
                            + "Flag them as mentor-eligible before assigning students.");
        }

        Integer oldMentorId = student.getMentorFaculty() != null ? student.getMentorFaculty().getId() : null;
        student.setMentorFaculty(mentor);
        Student saved = studentRepository.save(student);

        auditLogService.record(actingUserId, "ASSIGN_MENTOR", "students", student.getId(),
                oldMentorId != null ? ("mentor_faculty_id=" + oldMentorId) : null,
                "mentor_faculty_id=" + mentor.getId(), null, null);

        notificationService.notify(mentor.getUser().getId(), "New mentee assigned",
                student.getName() + " (" + student.getRegisterNo() + ") has been assigned to you as a mentee.",
                Notification.NotificationType.info);

        return StudentResponse.from(saved);
    }
}
