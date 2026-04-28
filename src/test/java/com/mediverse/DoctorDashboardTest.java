package com.mediverse;

import com.mediverse.entity.User;
import com.mediverse.entity.Role;
import com.mediverse.entity.Doctor;
import com.mediverse.repository.UserRepository;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class DoctorDashboardTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    public void testDoctorDashboardData() {
        // Find a doctor user
        Optional<User> doctorUserOpt = userRepository.findByEmail("sarah.johnson@mediverse.com");
        
        if (doctorUserOpt.isPresent()) {
            User doctorUser = doctorUserOpt.get();
            System.out.println("Found doctor user: " + doctorUser.getEmail());
            
            // Find the Doctor entity
            Optional<Doctor> doctorOpt = doctorRepository.findByUserId(doctorUser.getId());
            
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                System.out.println("Found doctor entity with ID: " + doctor.getId());
                
                // Test the appointment query
                try {
                    var appointments = appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId());
                    System.out.println("Found " + appointments.size() + " appointments for doctor");
                    
                    // Test status counts
                    long completedCount = appointments.stream()
                        .filter(apt -> "COMPLETED".equals(apt.getStatus().name()))
                        .count();
                    
                    long scheduledCount = appointments.stream()
                        .filter(apt -> "SCHEDULED".equals(apt.getStatus().name()))
                        .count();
                    
                    System.out.println("Completed appointments: " + completedCount);
                    System.out.println("Scheduled appointments: " + scheduledCount);
                    
                    // Test patient count
                    long uniquePatientsCount = appointments.stream()
                        .map(apt -> apt.getPatient().getId())
                        .distinct()
                        .count();
                    
                    System.out.println("Unique patients: " + uniquePatientsCount);
                    
                } catch (Exception e) {
                    System.err.println("Error testing appointments: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Doctor entity not found for user: " + doctorUser.getId());
            }
        } else {
            System.err.println("Doctor user not found");
        }
    }
}
