package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(Role.RoleName name);
}
