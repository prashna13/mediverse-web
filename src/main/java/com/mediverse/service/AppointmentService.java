package com.mediverse.service;

import com.mediverse.dto.AppointmentBookingDTO;
import com.mediverse.entity.*;
import com.mediverse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    /**
     * Book a new appointment for a patient
     */
    public Appointment bookAppointment(AppointmentBookingDTO bookingDTO, String patientEmail) {
        // Find the patient
        Optional<Patient> patientOpt = patientRepository.findByUserEmail(patientEmail);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient not found");
        }
        
        // Find the doctor
        Optional<Doctor> doctorOpt = doctorRepository.findById(bookingDTO.getDoctorId());
        if (doctorOpt.isEmpty()) {
            throw new RuntimeException("Doctor not found");
        }
        
        Patient patient = patientOpt.get();
        Doctor doctor = doctorOpt.get();
        
        // Combine date and time
        LocalDateTime appointmentDateTime = LocalDateTime.of(
            bookingDTO.getAppointmentDate(), 
            bookingDTO.getAppointmentTime()
        );
        
        // Check if the appointment slot is available
        if (!isAppointmentSlotAvailable(doctor.getId(), appointmentDateTime)) {
            throw new RuntimeException("This appointment slot is not available");
        }
        
        // Check if it's within doctor's working hours
        if (!isWithinWorkingHours(doctor, bookingDTO.getAppointmentTime())) {
            throw new RuntimeException("Appointment time is outside doctor's working hours");
        }
        
        // Create new appointment
        Appointment appointment = new Appointment(patient, doctor, appointmentDateTime);
        appointment.setReason(bookingDTO.getReason());
        appointment.setNotes(bookingDTO.getNotes());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Check if an appointment slot is available for a doctor
     */
    public boolean isAppointmentSlotAvailable(Long doctorId, LocalDateTime appointmentDateTime) {
        LocalDateTime startTime = appointmentDateTime;
        LocalDateTime endTime = appointmentDateTime.plusMinutes(30); // Default 30-minute slots
        
        List<Appointment> conflictingAppointments = appointmentRepository
            .findByDoctorIdAndAppointmentDateTimeBetween(doctorId, startTime.minusMinutes(29), endTime.minusMinutes(1));
        
        return conflictingAppointments.stream()
            .noneMatch(app -> app.getStatus() != AppointmentStatus.CANCELLED);
    }
    
    /**
     * Check if appointment time is within doctor's working hours
     */
    public boolean isWithinWorkingHours(Doctor doctor, LocalTime appointmentTime) {
        if (doctor.getAvailabilityStartTime() == null || doctor.getAvailabilityEndTime() == null) {
            return true; // No restrictions set
        }
        
        return !appointmentTime.isBefore(doctor.getAvailabilityStartTime()) && 
               !appointmentTime.isAfter(doctor.getAvailabilityEndTime().minusMinutes(30));
    }
    
    /**
     * Get available appointment slots for a doctor on a specific date
     */
    public List<LocalTime> getAvailableSlots(Long doctorId, java.time.LocalDate date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return List.of();
        }
        
        Doctor doctor = doctorOpt.get();
        LocalTime startTime = doctor.getAvailabilityStartTime() != null ? 
            doctor.getAvailabilityStartTime() : LocalTime.of(9, 0);
        LocalTime endTime = doctor.getAvailabilityEndTime() != null ? 
            doctor.getAvailabilityEndTime() : LocalTime.of(17, 0);
        
        List<LocalTime> allSlots = generateTimeSlots(startTime, endTime);
        
        // Filter out booked slots
        List<Appointment> bookedAppointments = appointmentRepository
            .findByDoctorIdAndAppointmentDateTimeBetween(
                doctorId, 
                date.atStartOfDay(), 
                date.atTime(23, 59, 59)
            );
        
        return allSlots.stream()
            .filter(slot -> {
                LocalDateTime slotDateTime = date.atTime(slot);
                return bookedAppointments.stream()
                    .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
                    .noneMatch(app -> 
                        app.getAppointmentDateTime().equals(slotDateTime) ||
                        (app.getAppointmentDateTime().isBefore(slotDateTime.plusMinutes(30)) &&
                         app.getAppointmentDateTime().isAfter(slotDateTime.minusMinutes(30)))
                    );
            })
            .toList();
    }
    
    /**
     * Generate time slots with 30-minute intervals
     */
    private List<LocalTime> generateTimeSlots(LocalTime startTime, LocalTime endTime) {
        List<LocalTime> slots = new java.util.ArrayList<>();
        LocalTime current = startTime;
        
        while (!current.isAfter(endTime.minusMinutes(30))) {
            slots.add(current);
            current = current.plusMinutes(30);
        }
        
        return slots;
    }
    
    /**
     * Cancel an appointment
     */
    public boolean cancelAppointment(Long appointmentId, String userEmail) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return false;
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // Check if the user is the patient who booked the appointment
        if (!appointment.getPatient().getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You can only cancel your own appointments");
        }
        
        // Check if appointment can be cancelled (not in the past or already completed)
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("This appointment cannot be cancelled");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return true;
    }
    
    /**
     * Get patient's appointments
     */
    public List<Appointment> getPatientAppointments(String patientEmail) {
        Optional<Patient> patientOpt = patientRepository.findByUserEmail(patientEmail);
        if (patientOpt.isEmpty()) {
            return List.of();
        }
        
        return appointmentRepository.findByPatientIdOrderByDateDesc(patientOpt.get().getId());
    }
    
    /**
     * Get doctor's appointments
     */
    public List<Appointment> getDoctorAppointments(String doctorEmail) {
        Optional<Doctor> doctorOpt = doctorRepository.findByUserEmail(doctorEmail);
        if (doctorOpt.isEmpty()) {
            return List.of();
        }
        
        return appointmentRepository.findByDoctorIdOrderByDateAsc(doctorOpt.get().getId());
    }
}
