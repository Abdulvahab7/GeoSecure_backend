package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.ClassCoordinatorHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassCoordinatorHistoryRepository extends JpaRepository<ClassCoordinatorHistory, Integer> {

    /** Phase 2 addition: full coordinator-change audit trail for one class, newest first. */
    List<ClassCoordinatorHistory> findByClassEntity_IdOrderByAssignedAtDesc(Integer classId);

    /** Phase 2 addition: the currently-open history row (if any) for a class - used to close it out on the next change. */
    List<ClassCoordinatorHistory> findByClassEntity_IdAndReleasedAtIsNull(Integer classId);
}
