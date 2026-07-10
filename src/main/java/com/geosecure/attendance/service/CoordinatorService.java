package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.CoordinatorAssignRequest;
import com.geosecure.attendance.dto.request.CoordinatorTransferRequest;
import com.geosecure.attendance.dto.response.ClassResponse;
import com.geosecure.attendance.dto.response.CoordinatorHistoryResponse;

import java.util.List;

/**
 * Coordinator rules:
 *   One class -> one coordinator. One faculty -> many classes.
 *   A coordinator may assign faculty/timetable/classrooms for their class,
 *   transfer ownership to another faculty, or leave coordination (no faculty
 *   assigned). An admin may always force a transfer regardless of the
 *   current coordinator's consent. Every change is recorded in
 *   class_coordinator_history for audit purposes.
 */
public interface CoordinatorService {

    /** Admin: first-time assignment of a coordinator to a class (or reassignment). */
    ClassResponse assign(CoordinatorAssignRequest request, Integer actingUserId);

    /** The current coordinator hands the class off to another faculty member. */
    ClassResponse transfer(CoordinatorTransferRequest request, Integer actingFacultyUserId);

    /** Admin forces a transfer regardless of who currently coordinates the class. */
    ClassResponse forceTransfer(CoordinatorTransferRequest request, Integer actingAdminUserId);

    /** The current coordinator steps down; the class is left without a coordinator. */
    ClassResponse leave(Integer classId, Integer actingFacultyUserId);

    List<CoordinatorHistoryResponse> historyForClass(Integer classId);
}
