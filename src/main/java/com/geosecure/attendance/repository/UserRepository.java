package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Role;
import com.geosecure.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Mirrors the query previously embedded in the original authentication logic:
 *   SELECT u.id, u.username, u.email, u.password_hash, u.is_active, r.name AS role
 *   FROM users u JOIN roles r ON r.id = u.role_id WHERE u.email = ?
 *
 * Because Role is @ManyToOne(FetchType.EAGER) on User, findByEmail() already
 * returns the joined role with zero extra queries.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByRegisterNo(String registerNo);

    boolean existsByEmployeeId(String employeeId);

    /** Equivalent of adminController.js getUsers() with optional ?role= filter */
    @Query("SELECT u FROM User u WHERE (:roleName IS NULL OR u.role.name = :roleName) ORDER BY u.createdAt DESC")
    List<User> findAllByOptionalRole(@Param("roleName") Role.RoleName roleName);

    long countByRole_Name(Role.RoleName roleName);

    /** Registration approval workflow: applicants awaiting an admin decision. */
    List<User> findAllByStatusOrderByCreatedAtAsc(User.Status status);
}
