package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.MentorAssignRequest;
import com.geosecure.attendance.dto.response.StudentResponse;

/**
 * Mentor rules: Student -> exactly one mentor (a FACULTY-role user with
 * faculty.is_mentor = true). A faculty member may mentor many students.
 * Mentor is a responsibility, never a role.
 */
public interface MentorService {

    StudentResponse assignMentor(MentorAssignRequest request, Integer actingUserId);
}
