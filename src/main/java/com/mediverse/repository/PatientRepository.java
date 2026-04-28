package com.mediverse.repository;

import com.mediverse.entity.Patient;
import com.mediverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByPatientId(String patientId);
    
    Optional<Patient> findByUserId(Long userId);
    
    Optional<Patient> findByUser(User user);
    
    @Query("SELECT p FROM Patient p WHERE p.user.email = :email")
    Optional<Patient> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT p FROM Patient p WHERE p.user.phoneNumber = :phoneNumber")
    Optional<Patient> findByUserPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT p FROM Patient p WHERE p.user.firstName LIKE %:name% OR p.user.lastName LIKE %:name%")
    List<Patient> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Patient p WHERE p.user.isActive = true")
    List<Patient> findActivePatients();
    
    boolean existsByPatientId(String patientId);
}
