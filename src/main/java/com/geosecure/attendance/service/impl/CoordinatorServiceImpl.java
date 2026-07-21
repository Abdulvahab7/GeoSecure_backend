package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.CoordinatorAssignRequest;
import com.geosecure.attendance.dto.request.CoordinatorTransferRequest;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.dto.response.CoordinatorHistoryResponse;
import com.geosecure.attendance.entity.ClassCoordinatorHistory;
import com.geosecure.attendance.entity.ClassEntity;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.entity.Notification;
import com.geosecure.attendance.entity.User;
import com.geosecure.attendance.exception.ForbiddenException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.ClassCoordinatorHistoryRepository;
import com.geosecure.attendance.repository.ClassRepository;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.repository.UserRepository;
import com.geosecure.attendance.service.AuditLogService;
import com.geosecure.attendance.service.CoordinatorService;
import com.geosecure.attendance.service.NotificationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Coordinator rules enforced here:
 *   - One class has at most one coordinator at a time (ClassEntity.coordinatorFaculty).
 *   - One faculty may coordinate many classes (no uniqueness constraint on faculty side).
 *   - The current coordinator may transfer ownership to another faculty member,
 *     or leave coordination outright (class ends up with no coordinator).
 *   - An admin may force a transfer regardless of the current coordinator's consent.
 *   - Every assignment/transfer/release is recorded in class_coordinator_history.
 */
@Service
public class CoordinatorServiceImpl implements CoordinatorService {

    private final ClassRepository classRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final ClassCoordinatorHistoryRepository historyRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public CoordinatorServiceImpl(ClassRepository classRepository,
                                   FacultyRepository facultyRepository,
                                   UserRepository userRepository,
                                   ClassCoordinatorHistoryRepository historyRepository,
                                   AuditLogService auditLogService,
                                   NotificationService notificationService) {
        this.classRepository = classRepository;
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ClassResponse assign(CoordinatorAssignRequest request, Integer actingUserId) {
        ClassEntity classEntity = requireClass(request.getClassId());
        Faculty newFaculty = requireFaculty(request.getFacultyId());
        User actingUser = requireUser(actingUserId);

        Faculty oldFaculty = classEntity.getCoordinatorFaculty();
        applyChange(classEntity, oldFaculty, newFaculty, actingUser, request.getReason());

        return ClassResponse.from(classEntity);
    }

    @Override
    @Transactional
    public ClassResponse transfer(CoordinatorTransferRequest request, Integer actingFacultyUserId) {
        ClassEntity classEntity = requireClass(request.getClassId());
        Faculty actingFaculty = facultyRepository.findByUser_Id(actingFacultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for this user."));

        if (classEntity.getCoordinatorFaculty() == null
                || !classEntity.getCoordinatorFaculty().getId().equals(actingFaculty.getId())) {
            throw new ForbiddenException("You are not the current coordinator of this class, so you cannot transfer it.");
        }

        Faculty newFaculty = request.getNewFacultyId() != null ? requireFaculty(request.getNewFacultyId()) : null;
        User actingUser = requireUser(actingFacultyUserId);

        applyChange(classEntity, actingFaculty, newFaculty, actingUser, request.getReason());

        return ClassResponse.from(classEntity);
    }

    @Override
    @Transactional
    public ClassResponse forceTransfer(CoordinatorTransferRequest request, Integer actingAdminUserId) {
        ClassEntity classEntity = requireClass(request.getClassId());
        Faculty oldFaculty = classEntity.getCoordinatorFaculty();
        Faculty newFaculty = request.getNewFacultyId() != null ? requireFaculty(request.getNewFacultyId()) : null;
        User actingUser = requireUser(actingAdminUserId);

        String reason = request.getReason() != null ? request.getReason() : "Forced transfer by admin";
        applyChange(classEntity, oldFaculty, newFaculty, actingUser, reason);

        return ClassResponse.from(classEntity);
    }

    @Override
    @Transactional
    public ClassResponse leave(Integer classId, Integer actingFacultyUserId) {
        ClassEntity classEntity = requireClass(classId);
        Faculty actingFaculty = facultyRepository.findByUser_Id(actingFacultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for this user."));

        if (classEntity.getCoordinatorFaculty() == null
                || !classEntity.getCoordinatorFaculty().getId().equals(actingFaculty.getId())) {
            throw new ForbiddenException("You are not the current coordinator of this class.");
        }

        User actingUser = requireUser(actingFacultyUserId);
        applyChange(classEntity, actingFaculty, null, actingUser, "Coordinator left the role");

        return ClassResponse.from(classEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinatorHistoryResponse> historyForClass(Integer classId) {
        requireClass(classId);
        return historyRepository.findByClassEntity_IdOrderByAssignedAtDesc(classId).stream()
                .map(CoordinatorHistoryResponse::from)
                .toList();
    }

    /** Central mutation point: updates the class, closes the previous history row, opens a new one. */
    private void applyChange(ClassEntity classEntity, Faculty oldFaculty, Faculty newFaculty, User actingUser, String reason) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        historyRepository.findByClassEntity_IdAndReleasedAtIsNull(classEntity.getId())
                .forEach(h -> {
                    h.setReleasedAt(now);
                    historyRepository.save(h);
                });

        classEntity.setCoordinatorFaculty(newFaculty);
        classRepository.save(classEntity);

        ClassCoordinatorHistory history = new ClassCoordinatorHistory();
        history.setClassEntity(classEntity);
        history.setOldFaculty(oldFaculty);
        history.setNewFaculty(newFaculty);
        history.setTransferredBy(actingUser);
        history.setTransferReason(reason);
        history.setAssignedAt(now);
        historyRepository.save(history);

        auditLogService.record(actingUser.getId(), "COORDINATOR_CHANGE", "classes", classEntity.getId(),
                oldFaculty != null ? ("coordinator_faculty_id=" + oldFaculty.getId()) : null,
                newFaculty != null ? ("coordinator_faculty_id=" + newFaculty.getId()) : "coordinator_faculty_id=null",
                null, null);

        if (newFaculty != null) {
            notificationService.notify(newFaculty.getUser().getId(), "Class coordination assigned",
                    "You are now the coordinator of " + classEntity.getName() + ".",
                    Notification.NotificationType.info);
        }
        if (oldFaculty != null && (newFaculty == null || !oldFaculty.getId().equals(newFaculty.getId()))) {
            notificationService.notify(oldFaculty.getUser().getId(), "Class coordination ended",
                    "You are no longer the coordinator of " + classEntity.getName() + ".",
                    Notification.NotificationType.info);
        }
    }

    private ClassEntity requireClass(Integer id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
    }

    private Faculty requireFaculty(Integer id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + id));
    }

    private User requireUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
