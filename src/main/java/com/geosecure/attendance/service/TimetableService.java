package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.TimetableRequest;
import com.geosecure.attendance.dto.response.TimetableResponse;
import com.geosecure.attendance.entity.Timetable;

import java.util.List;

public interface TimetableService {

    /** Admin/coordinator: create a slot. */
    TimetableResponse create(TimetableRequest request, Integer actingUserId);

    List<TimetableResponse> findForClass(Integer classId);

    List<TimetableResponse> todayForFaculty(Integer facultyId);

    List<TimetableResponse> weekForFaculty(Integer facultyId);

    /** Ownership check used by AttendanceService.generateQr(): throws if the slot isn't this faculty's. */
    Timetable requireOwnedByFaculty(Integer timetableId, Integer facultyId);
}
