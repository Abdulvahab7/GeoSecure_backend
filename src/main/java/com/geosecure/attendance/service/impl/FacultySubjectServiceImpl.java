package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.FacultySubjectRequest;
import com.geosecure.attendance.dto.response.FacultySubjectResponse;
import com.geosecure.attendance.entity.ClassEntity;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.entity.FacultySubject;
import com.geosecure.attendance.entity.Subject;
import com.geosecure.attendance.exception.DuplicateResourceException;
import com.geosecure.attendance.exception.ForbiddenException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.ClassRepository;
import com.geosecure.attendance.repository.FacultyRepository;
import com.geosecure.attendance.repository.FacultySubjectRepository;
import com.geosecure.attendance.repository.SubjectRepository;
import com.geosecure.attendance.service.FacultySubjectService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacultySubjectServiceImpl implements FacultySubjectService {

    private final FacultySubjectRepository facultySubjectRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;

    public FacultySubjectServiceImpl(FacultySubjectRepository facultySubjectRepository,
                                      FacultyRepository facultyRepository,
                                      SubjectRepository subjectRepository,
                                      ClassRepository classRepository) {
        this.facultySubjectRepository = facultySubjectRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.classRepository = classRepository;
    }

    @Override
    @Transactional
    public FacultySubjectResponse assign(FacultySubjectRequest request) {
        if (facultySubjectRepository.existsByFaculty_IdAndSubject_IdAndClassEntity_IdAndAcademicYear(
                request.getFacultyId(), request.getSubjectId(), request.getClassId(), request.getAcademicYear())) {
            throw new DuplicateResourceException("This faculty is already assigned to teach this subject to this class for " + request.getAcademicYear() + ".");
        }

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + request.getFacultyId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + request.getSubjectId()));
        ClassEntity classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + request.getClassId()));

        FacultySubject fs = new FacultySubject();
        fs.setFaculty(faculty);
        fs.setSubject(subject);
        fs.setClassEntity(classEntity);
        fs.setAcademicYear(request.getAcademicYear());

        return FacultySubjectResponse.from(facultySubjectRepository.save(fs));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultySubjectResponse> myAssignments(Integer facultyId) {
        return facultySubjectRepository.findByFacultyIdWithSubjectAndClass(facultyId).stream()
                .map(FacultySubjectResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FacultySubject requireOwnedByFaculty(Integer facultySubjectId, Integer facultyId) {
        return facultySubjectRepository.findByIdAndFaculty_Id(facultySubjectId, facultyId)
                .orElseThrow(() -> new ForbiddenException("This subject assignment does not belong to you."));
    }
}
