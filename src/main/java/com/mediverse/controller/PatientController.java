package com.mediverse.controller;

import com.mediverse.dto.AppointmentBookingDTO;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.Department;
import com.mediverse.entity.Doctor;
import com.mediverse.entity.Patient;
import com.mediverse.entity.User;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.DepartmentRepository;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.repository.PatientRepository;
import com.mediverse.service.AppointmentService;
import com.mediverse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();

        // Get the patient entity first
        Optional<Patient> patientOptional = patientRepository.findByUserId(currentUser.getId());
        if (patientOptional.isEmpty()) {
            return "redirect:/login?error=patient_not_found";
        }
        
        Patient patient = patientOptional.get();
        List<Appointment> patientAppointments = appointmentRepository.findByPatientIdOrderByDateDesc(patient.getId());
        
        model.addAttribute("user", currentUser);
        model.addAttribute("appointments", patientAppointments);
        model.addAttribute("totalAppointments", patientAppointments.size());
        
        return "patient/dashboard";
    }
    
    @GetMapping("/appointments")
    public String appointments(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        
        // Get the patient entity first
        Optional<Patient> patientOptional = patientRepository.findByUserId(currentUser.getId());
        if (patientOptional.isEmpty()) {
            return "redirect:/login?error=patient_not_found";
        }
        
        Patient patient = patientOptional.get();
        List<Appointment> patientAppointments = appointmentRepository.findByPatientIdOrderByDateDesc(patient.getId());
        
        model.addAttribute("user", currentUser);
        model.addAttribute("appointments", patientAppointments);
        
        return "patient/appointments";
    }
    
    @GetMapping("/book-appointment")
    public String bookAppointment(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        List<Department> departments = departmentRepository.findAll();
        List<Doctor> doctors = doctorRepository.findAll();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("departments", departments);
        model.addAttribute("doctors", doctors);
        model.addAttribute("appointmentBooking", new AppointmentBookingDTO());
        
        return "patient/book-appointment";
    }
    
    @PostMapping("/book-appointment")
    public String processBookAppointment(@Valid @ModelAttribute("appointmentBooking") AppointmentBookingDTO bookingDTO,
                                       BindingResult bindingResult,
                                       Authentication authentication,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        
        if (bindingResult.hasErrors()) {
            List<Department> departments = departmentRepository.findAll();
            List<Doctor> doctors = doctorRepository.findAll();
            
            model.addAttribute("user", currentUser);
            model.addAttribute("departments", departments);
            model.addAttribute("doctors", doctors);
            return "patient/book-appointment";
        }
        
        try {
            Appointment appointment = appointmentService.bookAppointment(bookingDTO, currentUser.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Appointment booked successfully! Your appointment ID is: " + appointment.getId());
            return "redirect:/patient/appointments";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/book-appointment";
        }
    }
    
    @GetMapping("/api/doctors/{departmentId}")
    @ResponseBody
    public ResponseEntity<List<Doctor>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<Doctor> doctors = doctorRepository.findByDepartmentId(departmentId);
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/api/doctors/{doctorId}/slots")
    @ResponseBody
    public ResponseEntity<List<LocalTime>> getAvailableSlots(@PathVariable Long doctorId, 
                                                           @RequestParam String date) {
        try {
            LocalDate appointmentDate = LocalDate.parse(date);
            List<LocalTime> slots = appointmentService.getAvailableSlots(doctorId, appointmentDate);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        model.addAttribute("user", currentUser);
        
        // Find or create patient record
        Optional<Patient> patientOptional = patientRepository.findByUser(currentUser);
        if (patientOptional.isPresent()) {
            model.addAttribute("patient", patientOptional.get());
        } else {
            // Create a new patient record if it doesn't exist
            model.addAttribute("patient", new Patient());
        }
        
        return "patient/profile";
    }
    
    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        model.addAttribute("user", currentUser);
        
        // Find or create patient record
        Optional<Patient> patientOptional = patientRepository.findByUser(currentUser);
        Patient patient;
        if (patientOptional.isPresent()) {
            patient = patientOptional.get();
        } else {
            // Create a new patient record if it doesn't exist
            patient = new Patient();
            patient.setUser(currentUser);
            patient.setPatientId("PAT" + String.format("%06d", currentUser.getId()));
        }
        model.addAttribute("patient", patient);
        
        return "patient/profile-edit";
    }
    
    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute Patient patient, 
                              BindingResult result,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        
        try {
            // Find existing patient record or create new one
            Optional<Patient> existingPatientOptional = patientRepository.findByUser(currentUser);
            Patient existingPatient;
            
            if (existingPatientOptional.isPresent()) {
                existingPatient = existingPatientOptional.get();
            } else {
                existingPatient = new Patient();
                existingPatient.setUser(currentUser);
                existingPatient.setPatientId("PAT" + String.format("%06d", currentUser.getId()));
            }
            
            // Update patient information
            existingPatient.setDateOfBirth(patient.getDateOfBirth());
            existingPatient.setGender(patient.getGender());
            existingPatient.setBloodGroup(patient.getBloodGroup());
            existingPatient.setEmergencyContactName(patient.getEmergencyContactName());
            existingPatient.setEmergencyContactPhone(patient.getEmergencyContactPhone());
            existingPatient.setMedicalHistory(patient.getMedicalHistory());
            existingPatient.setAllergies(patient.getAllergies());
            existingPatient.setCurrentMedications(patient.getCurrentMedications());
            existingPatient.setAddress(patient.getAddress());
            
            // Update user information
            currentUser.setPhoneNumber(patient.getUser().getPhoneNumber());
            
            // Save both user and patient
            userService.updateUser(currentUser);
            patientRepository.save(existingPatient);
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/patient/profile";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/patient/profile/edit";
        }
    }
    
    @GetMapping("/edit-appointment/{id}")
    public String editAppointment(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        Optional<Patient> patientOptional = patientRepository.findByUserId(currentUser.getId());
        if (patientOptional.isEmpty()) {
            return "redirect:/login?error=patient_not_found";
        }
        
        // Get the appointment and verify it belongs to the patient
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
        if (appointmentOptional.isEmpty() || 
            !appointmentOptional.get().getPatient().getId().equals(patientOptional.get().getId())) {
            return "redirect:/patient/appointments?error=appointment_not_found";
        }
        
        // Check if the appointment can be edited (only scheduled appointments can be edited)
        Appointment appointment = appointmentOptional.get();
        if (!appointment.getStatus().name().equals("SCHEDULED")) {
            return "redirect:/patient/appointments?error=cannot_edit_completed_or_cancelled";
        }
        
        List<Department> departments = departmentRepository.findAll();
        List<Doctor> doctors = doctorRepository.findAll();
        
        // Create DTO for the form
        AppointmentBookingDTO bookingDTO = new AppointmentBookingDTO();
        bookingDTO.setDoctorId(appointment.getDoctor().getId());
        bookingDTO.setAppointmentDate(appointment.getAppointmentDateTime().toLocalDate());
        bookingDTO.setAppointmentTime(appointment.getAppointmentDateTime().toLocalTime());
        bookingDTO.setReason(appointment.getReason());
        
        model.addAttribute("user", currentUser);
        model.addAttribute("departments", departments);
        model.addAttribute("doctors", doctors);
        model.addAttribute("appointmentBooking", bookingDTO);
        model.addAttribute("appointmentId", appointment.getId());
        
        return "patient/edit-appointment";
    }
    
    @PostMapping("/update-appointment")
    public String updateAppointment(@RequestParam("appointmentId") String idStr,
                                  @RequestParam("doctorId") Long doctorId,
                                  @RequestParam("appointmentDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
                                  @RequestParam("appointmentTime") @DateTimeFormat(pattern = "HH:mm") LocalTime appointmentTime,
                                  @RequestParam("reason") String reason,
                                  @RequestParam(value = "notes", required = false) String notes,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        
        // Convert string ID to Long
        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid appointment ID");
            return "redirect:/patient/appointments";
        }
        
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
            return "redirect:/login?error=unauthorized";
        }

        User currentUser = userOptional.get();
        Optional<Patient> patientOptional = patientRepository.findByUserId(currentUser.getId());
        if (patientOptional.isEmpty()) {
            return "redirect:/login?error=patient_not_found";
        }
        
        // Get the existing appointment
        Optional<Appointment> existingAppointmentOptional = appointmentRepository.findById(id);
        if (existingAppointmentOptional.isEmpty() || 
            !existingAppointmentOptional.get().getPatient().getId().equals(patientOptional.get().getId())) {
            return "redirect:/patient/appointments?error=appointment_not_found";
        }
        
        // Check if the appointment can be edited
        Appointment existingAppointment = existingAppointmentOptional.get();
        if (!existingAppointment.getStatus().name().equals("SCHEDULED")) {
            return "redirect:/patient/appointments?error=cannot_edit_completed_or_cancelled";
        }
        
        try {
            // Update the appointment
            existingAppointment.setDoctor(doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found")));
                
            // Combine date and time into a single LocalDateTime
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointmentDate, 
                appointmentTime
            );
            existingAppointment.setAppointmentDateTime(appointmentDateTime);
            existingAppointment.setReason(reason);
            existingAppointment.setUpdatedAt(LocalDateTime.now());
            
            appointmentRepository.save(existingAppointment);
            
            redirectAttributes.addFlashAttribute("successMessage", "Appointment updated successfully!");
            return "redirect:/patient/appointments";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating appointment: " + e.getMessage());
            return "redirect:/patient/edit-appointment/" + id;
        }
    }
    
    @DeleteMapping("/appointments/{id}")
    @ResponseBody
    public ResponseEntity<String> cancelAppointment(
            @PathVariable("id") Long id,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isEmpty() || !userOptional.get().getRole().name().equals("PATIENT")) {
                return ResponseEntity.status(401).body("Unauthorized access");
            }
            
            // Get the appointment and verify it belongs to the patient
            Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
            if (appointmentOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Appointment appointment = appointmentOptional.get();
            if (!appointment.getPatient().getUser().getEmail().equals(email)) {
                return ResponseEntity.status(403).body("You don't have permission to cancel this appointment");
            }
            
            // Check if the appointment can be cancelled
            if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
                return ResponseEntity.badRequest().body("Only scheduled appointments can be cancelled");
            }
            
            // Update the appointment status to CANCELLED
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appointment);
            
            return ResponseEntity.ok("Appointment cancelled successfully");
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error cancelling appointment: " + e.getMessage());
        }
    }
}
