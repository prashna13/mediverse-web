package com.mediverse.repository;

import com.mediverse.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByName(String name);
    
    List<Department> findByIsActiveTrue();
    
    @Query("SELECT d FROM Department d WHERE d.isActive = true ORDER BY d.name")
    List<Department> findActiveDepartmentsOrderByName();
    
    boolean existsByName(String name);
}
