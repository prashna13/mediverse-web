package com.mediverse.repository;

import com.mediverse.entity.Doctor;
import com.mediverse.entity.Department;
import com.mediverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByDoctorId(String doctorId);
    
    Optional<Doctor> findByUserId(Long userId);
    
    Optional<Doctor> findByUser(User user);
    
    @Query("SELECT d FROM Doctor d WHERE d.user.email = :email")
    Optional<Doctor> findByUserEmail(@Param("email") String email);
    
    List<Doctor> findByDepartment(Department department);
    
    List<Doctor> findBySpecialization(String specialization);
    
    @Query("SELECT d FROM Doctor d WHERE d.isAvailable = true AND d.user.isActive = true")
    List<Doctor> findAvailableDoctors();
    
    @Query("SELECT d FROM Doctor d WHERE d.department.id = :departmentId AND d.isAvailable = true")
    List<Doctor> findAvailableDoctorsByDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT d FROM Doctor d WHERE d.department.id = :departmentId")
    List<Doctor> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT d FROM Doctor d WHERE d.user.firstName LIKE %:name% OR d.user.lastName LIKE %:name%")
    List<Doctor> findByNameContaining(@Param("name") String name);
    
    boolean existsByDoctorId(String doctorId);
    
    boolean existsByLicenseNumber(String licenseNumber);
}
