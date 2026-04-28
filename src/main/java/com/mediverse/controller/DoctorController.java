package com.mediverse.controller;

import com.mediverse.entity.Appointment;
import com.mediverse.entity.Doctor;
import com.mediverse.entity.User;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("DOCTOR")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        
        // Get the Doctor entity for this user
        Optional<Doctor> doctorOptional = doctorRepository.findByUserId(currentUser.getId());
        if (doctorOptional.isEmpty()) {
            return "redirect:/login?error=doctor_not_found";
        }
        
        Doctor doctor = doctorOptional.get();

        // Get doctor's appointments using the correct doctor ID
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId());
        
        // Get today's appointments
        List<Appointment> todaysAppointments = appointmentRepository.findTodaysAppointments();
        
        // Calculate status counts
        long completedCount = doctorAppointments.stream()
            .filter(apt -> "COMPLETED".equals(apt.getStatus().name()))
            .count();
        
        long scheduledCount = doctorAppointments.stream()
            .filter(apt -> "SCHEDULED".equals(apt.getStatus().name()))
            .count();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", doctorAppointments);
        model.addAttribute("todaysAppointments", todaysAppointments);
        model.addAttribute("totalAppointments", doctorAppointments.size());
        model.addAttribute("todaysCount", todaysAppointments.size());
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("scheduledCount", scheduledCount);
        
        return "doctor/dashboard";
    }
    
    @GetMapping("/appointments")
    public String appointments(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("DOCTOR")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        
        // Get the Doctor entity for this user
        Optional<Doctor> doctorOptional = doctorRepository.findByUserId(currentUser.getId());
        if (doctorOptional.isEmpty()) {
            return "redirect:/login?error=doctor_not_found";
        }
        
        Doctor doctor = doctorOptional.get();
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId());
        
        model.addAttribute("user", currentUser);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", doctorAppointments);
        
        return "doctor/appointments";
    }
    
    @GetMapping("/schedule")
    public String schedule(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("DOCTOR")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        model.addAttribute("user", currentUser);
        
        return "doctor/schedule";
    }
    
    @GetMapping("/patients")
    public String patients(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("DOCTOR")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        
        // Get the Doctor entity for this user
        Optional<Doctor> doctorOptional = doctorRepository.findByUserId(currentUser.getId());
        if (doctorOptional.isEmpty()) {
            return "redirect:/login?error=doctor_not_found";
        }
        
        Doctor doctor = doctorOptional.get();
        
        // Get unique patients from appointments
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId());
        
        // Calculate unique patients count
        long uniquePatientsCount = doctorAppointments.stream()
            .map(apt -> apt.getPatient().getId())
            .distinct()
            .count();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", doctorAppointments);
        model.addAttribute("uniquePatientsCount", uniquePatientsCount);
        model.addAttribute("patientVisitCount", 1); // This would need to be calculated per patient
        
        return "doctor/patients";
    }
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("DOCTOR")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        
        // Get the Doctor entity for this user
        Optional<Doctor> doctorOptional = doctorRepository.findByUserId(currentUser.getId());
        if (doctorOptional.isEmpty()) {
            return "redirect:/login?error=doctor_not_found";
        }
        
        Doctor doctor = doctorOptional.get();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("doctor", doctor);
        
        return "doctor/profile";
    }
}
