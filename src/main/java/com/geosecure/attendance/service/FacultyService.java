package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.CreateFacultyRequest;
import com.geosecure.attendance.dto.request.UpdateFacultyRequest;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.dto.response.FacultyResponse;
import com.geosecure.attendance.dto.response.StudentResponse;
import com.geosecure.attendance.entity.Faculty;

import java.util.List;

public interface FacultyService {

    List<FacultyResponse> findAll();

    FacultyResponse findById(Integer id);

    FacultyResponse findByUserId(Integer userId);

    /** Resolves the Faculty entity for the currently authenticated user - used by other services for ownership checks. */
    Faculty requireByUserId(Integer userId);

    FacultyResponse create(CreateFacultyRequest request);

    FacultyResponse update(Integer id, UpdateFacultyRequest request);

    List<FacultyResponse> findMentorEligible();

    List<StudentResponse> myMentees(Integer facultyId);

    List<ClassResponse> myCoordinatedClasses(Integer facultyId);
}
