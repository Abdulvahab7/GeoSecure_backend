package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.FacultySubjectRequest;
import com.geosecure.attendance.dto.response.FacultySubjectResponse;
import com.geosecure.attendance.entity.FacultySubject;

import java.util.List;

public interface FacultySubjectService {

    FacultySubjectResponse assign(FacultySubjectRequest request);

    List<FacultySubjectResponse> myAssignments(Integer facultyId);

    /** Ownership check used by AttendanceService/TimetableService: throws if not owned. */
    FacultySubject requireOwnedByFaculty(Integer facultySubjectId, Integer facultyId);
}
