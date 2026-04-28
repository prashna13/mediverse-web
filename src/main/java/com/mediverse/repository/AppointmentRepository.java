package com.mediverse.repository;

import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.Doctor;
import com.mediverse.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    Optional<Appointment> findByAppointmentId(String appointmentId);
    
    List<Appointment> findByPatient(Patient patient);
    
    List<Appointment> findByDoctor(Doctor doctor);
    
    List<Appointment> findByStatus(AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByPatientIdOrderByDateDesc(@Param("patientId") Long patientId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findByDoctorIdOrderByDateAsc(@Param("doctorId") Long doctorId);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByAppointmentDateTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByDoctorIdAndDateBetween(@Param("doctorId") Long doctorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDateTime BETWEEN :startTime AND :endTime")
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetween(@Param("doctorId") Long doctorId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientIdAndDateBetween(@Param("patientId") Long patientId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    List<Appointment> findTop10ByOrderByAppointmentDateTimeDesc();
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDateTime) = CURRENT_DATE ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findTodaysAppointments();
    
    boolean existsByAppointmentId(String appointmentId);
}
