package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.TimetableRequest;
import com.geosecure.attendance.dto.response.TimetableResponse;
import com.geosecure.attendance.entity.ClassEntity;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.entity.FacultySubject;
import com.geosecure.attendance.entity.Subject;
import com.geosecure.attendance.entity.Timetable;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ForbiddenException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.ClassRepository;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.repository.FacultySubjectRepository;
import com.geosecure.attendance.repository.SubjectRepository;
import com.geosecure.attendance.repository.TimetableRepository;
import com.geosecure.attendance.service.TimetableService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final ClassRepository classRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final FacultySubjectRepository facultySubjectRepository;

    public TimetableServiceImpl(TimetableRepository timetableRepository,
                                 ClassRepository classRepository,
                                 FacultyRepository facultyRepository,
                                 SubjectRepository subjectRepository,
                                 FacultySubjectRepository facultySubjectRepository) {
        this.timetableRepository = timetableRepository;
        this.classRepository = classRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.facultySubjectRepository = facultySubjectRepository;
    }

    @Override
    @Transactional
    public TimetableResponse create(TimetableRequest request, Integer actingUserId) {
        if (timetableRepository.existsByClassEntity_IdAndDayOfWeekAndSessionNumber(
                request.getClassId(), request.getDayOfWeek(), request.getSessionNumber())) {
            throw new DuplicateResourceException("A timetable slot already exists for this class on "
                    + request.getDayOfWeek() + " session " + request.getSessionNumber() + ".");
        }

        ClassEntity classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + request.getClassId()));
        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + request.getFacultyId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + request.getSubjectId()));

        Timetable timetable = new Timetable();
        timetable.setClassEntity(classEntity);
        timetable.setFaculty(faculty);
        timetable.setSubject(subject);
        timetable.setDayOfWeek(request.getDayOfWeek());
        timetable.setSessionNumber(request.getSessionNumber());
        timetable.setRoomNumber(request.getRoomNumber());

        if (request.getFacultySubjectId() != null) {
            FacultySubject fs = facultySubjectRepository.findById(request.getFacultySubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty-subject assignment not found: " + request.getFacultySubjectId()));
            timetable.setFacultySubject(fs);
        }

        return TimetableResponse.from(timetableRepository.save(timetable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> findForClass(Integer classId) {
        return timetableRepository.findAllForAdmin(classId).stream().map(TimetableResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> todayForFaculty(Integer facultyId) {
        Timetable.DayOfWeek today = mapToSchoolDay(java.time.LocalDate.now().getDayOfWeek());
        if (today == null) {
            return List.of();
        }
        return timetableRepository.findTodayScheduleForFaculty(facultyId, today).stream()
                .map(TimetableResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> weekForFaculty(Integer facultyId) {
        return timetableRepository.findFullWeekForFaculty(facultyId).stream()
                .sorted(Comparator.comparingInt((Timetable t) -> t.getDayOfWeek().ordinal())
                        .thenComparing(Timetable::getSessionNumber))
                .map(TimetableResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Timetable requireOwnedByFaculty(Integer timetableId, Integer facultyId) {
        return timetableRepository.findByIdAndFacultyId(timetableId, facultyId)
                .orElseThrow(() -> new ForbiddenException("This timetable slot does not belong to you."));
    }

    /** java.time.DayOfWeek (Mon..Sun) -> Timetable.DayOfWeek (Mon..Sat); null on Sunday (no classes modeled). */
    private Timetable.DayOfWeek mapToSchoolDay(java.time.DayOfWeek day) {
        return switch (day) {
            case MONDAY -> Timetable.DayOfWeek.Monday;
            case TUESDAY -> Timetable.DayOfWeek.Tuesday;
            case WEDNESDAY -> Timetable.DayOfWeek.Wednesday;
            case THURSDAY -> Timetable.DayOfWeek.Thursday;
            case FRIDAY -> Timetable.DayOfWeek.Friday;
            case SATURDAY -> Timetable.DayOfWeek.Saturday;
            case SUNDAY -> null;
        };
    }
}
