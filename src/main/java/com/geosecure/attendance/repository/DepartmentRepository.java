package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    Optional<Department> findByCode(String code);

    List<Department> findAllByOrderByNameAsc();
}
