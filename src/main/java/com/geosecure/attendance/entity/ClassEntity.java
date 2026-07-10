package com.geosecure.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Maps to: classes table.
 * Represents a batch/section of students (e.g., CSE-A 2026).
 * Named ClassEntity (not "Class") to avoid colliding with java.lang.Class.
 * coordinatorFaculty is a FACULTY-role user assigned the class-coordinator
 * responsibility; it is not a role of its own.
 */
@Entity
@Table(
        name = "classes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_class",
                columnNames = {"department_id", "semester", "section", "academic_year"}
        )
)
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "section", nullable = false, length = 5)
    private String section;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_faculty_id")
    private Faculty coordinatorFaculty;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public ClassEntity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Faculty getCoordinatorFaculty() {
        return coordinatorFaculty;
    }

    public void setCoordinatorFaculty(Faculty coordinatorFaculty) {
        this.coordinatorFaculty = coordinatorFaculty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
