package com.mediverse.controller;

import com.mediverse.entity.*;
import com.mediverse.service.UserService;
import com.mediverse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BranchRepository branchRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get dashboard statistics
        long totalPatients = userService.countUsersByRole(Role.PATIENT);
        long totalDoctors = userService.countUsersByRole(Role.DOCTOR);
        long totalDepartments = departmentRepository.count();
        long totalAppointments = appointmentRepository.count();
        
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("totalAppointments", totalAppointments);
        
        // Get recent appointments
        model.addAttribute("recentAppointments", appointmentRepository.findTop10ByOrderByAppointmentDateTimeDesc());
        
        return "admin/dashboard";
    }

    // ==================== USER MANAGEMENT ====================
    
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        model.addAttribute("departments", departmentRepository.findByIsActiveTrue());
        return "admin/user-new";
    }

    @PostMapping("/users/new")
    public String createUser(@ModelAttribute User user, BindingResult result, 
                           @RequestParam(required = false) Long departmentId,
                           Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("departments", departmentRepository.findByIsActiveTrue());
            return "admin/user-new";
        }

        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email address is already registered");
            model.addAttribute("roles", Role.values());
            model.addAttribute("departments", departmentRepository.findByIsActiveTrue());
            return "admin/user-new";
        }

        try {
            user.setIsActive(true);
            // Save user with hashed password
            User savedUser = userService.saveUser(user);
            
            // Create corresponding profile based on role
            if (user.getRole() == Role.PATIENT) {
                Patient patient = new Patient(savedUser);
                patientRepository.save(patient);
            } else if (user.getRole() == Role.DOCTOR) {
                // Initialize doctor with a default specialization
                String defaultSpecialization = "General Medicine";
                Doctor doctor = new Doctor(savedUser, defaultSpecialization);
                doctorRepository.save(doctor);
            }
            
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error creating user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("departments", departmentRepository.findByIsActiveTrue());
            return "admin/user-new";
        }
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            model.addAttribute("roles", Role.values());
            return "admin/user-edit";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, 
                           Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> existingUserOpt = userService.findById(id);
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                // Update only the necessary fields
                existingUser.setFirstName(user.getFirstName());
                existingUser.setLastName(user.getLastName());
                existingUser.setEmail(user.getEmail());
                existingUser.setPhoneNumber(user.getPhoneNumber());
                existingUser.setRole(user.getRole());
                existingUser.setIsActive(user.getIsActive());
                
                // Only update password if a new one was provided
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    existingUser.setPassword(user.getPassword());
                }
                
                userService.updateUser(existingUser);
                redirectAttributes.addFlashAttribute("success", "User updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // First, check if the user exists
            User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
            // Delete associated doctor or patient record first
            if (user.getRole() == Role.DOCTOR) {
                doctorRepository.findByUser(user).ifPresent(doctor -> doctorRepository.delete(doctor));
            } else if (user.getRole() == Role.PATIENT) {
                patientRepository.findByUser(user).ifPresent(patient -> patientRepository.delete(patient));
            }
            
            // Now delete the user
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.activateUser(id);
            redirectAttributes.addFlashAttribute("success", "User activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error activating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deactivateUser(id);
            redirectAttributes.addFlashAttribute("success", "User deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deactivating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ==================== PATIENT MANAGEMENT ====================
    
    @GetMapping("/patients")
    public String patients(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "admin/patients";
    }

    @GetMapping("/patients/{id}/edit")
    public String editPatient(@PathVariable Long id, Model model) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            model.addAttribute("patient", patientOpt.get());
            return "admin/patient-edit";
        }
        return "redirect:/admin/patients";
    }

    @PostMapping("/patients/{id}/edit")
    public String updatePatient(@PathVariable Long id, @ModelAttribute Patient patient, RedirectAttributes redirectAttributes) {
        try {
            Optional<Patient> existingPatientOpt = patientRepository.findById(id);
            if (existingPatientOpt.isPresent()) {
                Patient existingPatient = existingPatientOpt.get();
                existingPatient.setDateOfBirth(patient.getDateOfBirth());
                existingPatient.setGender(patient.getGender());
                existingPatient.setBloodGroup(patient.getBloodGroup());
                existingPatient.setAddress(patient.getAddress());
                existingPatient.setEmergencyContactName(patient.getEmergencyContactName());
                existingPatient.setEmergencyContactPhone(patient.getEmergencyContactPhone());
                existingPatient.setMedicalHistory(patient.getMedicalHistory());
                existingPatient.setAllergies(patient.getAllergies());
                existingPatient.setCurrentMedications(patient.getCurrentMedications());
                
                patientRepository.save(existingPatient);
                redirectAttributes.addFlashAttribute("success", "Patient updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating patient: " + e.getMessage());
        }
        return "redirect:/admin/patients";
    }

    @PostMapping("/patients/{id}/delete")
    public String deletePatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            patientRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Patient deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting patient: " + e.getMessage());
        }
        return "redirect:/admin/patients";
    }

    // ==================== DOCTOR MANAGEMENT ====================
    
    @GetMapping("/doctors")
    public String doctors(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/doctors";
    }

    @GetMapping("/doctors/{id}/edit")
    public String editDoctor(@PathVariable Long id, Model model) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isPresent()) {
            model.addAttribute("doctor", doctorOpt.get());
            model.addAttribute("departments", departmentRepository.findAll());
            return "admin/doctor-edit";
        }
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/edit")
    public String updateDoctor(@PathVariable Long id, @ModelAttribute Doctor doctor, RedirectAttributes redirectAttributes) {
        try {
            Optional<Doctor> existingDoctorOpt = doctorRepository.findById(id);
            if (existingDoctorOpt.isPresent()) {
                Doctor existingDoctor = existingDoctorOpt.get();
                existingDoctor.setSpecialization(doctor.getSpecialization());
                existingDoctor.setQualification(doctor.getQualification());
                existingDoctor.setExperienceYears(doctor.getExperienceYears());
                existingDoctor.setLicenseNumber(doctor.getLicenseNumber());
                existingDoctor.setConsultationFee(doctor.getConsultationFee());
                existingDoctor.setBio(doctor.getBio());
                existingDoctor.setDepartment(doctor.getDepartment());
                existingDoctor.setWorkingDays(doctor.getWorkingDays());
                existingDoctor.setAvailabilityStartTime(doctor.getAvailabilityStartTime());
                existingDoctor.setAvailabilityEndTime(doctor.getAvailabilityEndTime());
                existingDoctor.setIsAvailable(doctor.getIsAvailable());
                
                doctorRepository.save(existingDoctor);
                redirectAttributes.addFlashAttribute("success", "Doctor updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating doctor: " + e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Doctor deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting doctor: " + e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    // ==================== DEPARTMENT MANAGEMENT ====================
    
    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/departments";
    }

    @GetMapping("/departments/new")
    public String newDepartment(Model model) {
        model.addAttribute("department", new Department());
        return "admin/department-new";
    }

    @PostMapping("/departments/new")
    public String createDepartment(@ModelAttribute Department department, RedirectAttributes redirectAttributes) {
        try {
            departmentRepository.save(department);
            redirectAttributes.addFlashAttribute("success", "Department created successfully!");
            return "redirect:/admin/departments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating department: " + e.getMessage());
            return "admin/department-new";
        }
    }

    @GetMapping("/departments/{id}/edit")
    public String editDepartment(@PathVariable Long id, Model model) {
        Optional<Department> departmentOpt = departmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            model.addAttribute("department", departmentOpt.get());
            return "admin/department-edit";
        }
        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/{id}/edit")
    public String updateDepartment(@PathVariable Long id, 
                                 @Valid @ModelAttribute("department") Department department, 
                                 BindingResult result, 
                                 Model model, 
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Updating department with ID: " + id);
            System.out.println("Form data: " + department.toString());
            
            if (result.hasErrors()) {
                System.out.println("Form has errors: " + result.getAllErrors());
                model.addAttribute("department", department);
                return "admin/department-edit";
            }
            
            Optional<Department> existingDeptOpt = departmentRepository.findById(id);
            if (existingDeptOpt.isPresent()) {
                Department existingDept = existingDeptOpt.get();
                System.out.println("Existing department: " + existingDept.toString());
                
                existingDept.setName(department.getName());
                existingDept.setCode(department.getCode());
                existingDept.setDescription(department.getDescription());
                existingDept.setHeadDoctor(department.getHeadDoctor());
                existingDept.setEmail(department.getEmail());
                existingDept.setPhoneNumber(department.getPhoneNumber());
                existingDept.setIsActive(department.getIsActive());
                
                Department savedDept = departmentRepository.save(existingDept);
                System.out.println("Updated department: " + savedDept.toString());
                
                redirectAttributes.addFlashAttribute("success", "Department updated successfully!");
            } else {
                System.out.println("Department not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Department not found!");
            }
        } catch (Exception e) {
            System.out.println("Error updating department: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating department: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/{id}/delete")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Department deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting department: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/departments/{id}/view")
    public String viewDepartment(@PathVariable Long id, Model model) {
        Optional<Department> departmentOpt = departmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            Department department = departmentOpt.get();
            model.addAttribute("department", department);
            
            // Get doctors in this department
            List<Doctor> doctors = doctorRepository.findByDepartmentId(id);
            model.addAttribute("doctors", doctors);
            
            // Get appointment count for this department
            int appointmentCount = 0;
            for (Doctor doctor : doctors) {
                appointmentCount += appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId()).size();
            }
            model.addAttribute("appointmentCount", appointmentCount);
            
            return "admin/department-view";
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/departments/{id}/doctors")
    public String departmentDoctors(@PathVariable Long id, Model model) {
        Optional<Department> departmentOpt = departmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            Department department = departmentOpt.get();
            model.addAttribute("department", department);
            
            // Get doctors in this department
            List<Doctor> doctors = doctorRepository.findByDepartmentId(id);
            model.addAttribute("doctors", doctors);
            
            return "admin/department-doctors";
        }
        return "redirect:/admin/departments";
    }

    // ==================== APPOINTMENT MANAGEMENT ====================
    
    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", appointmentRepository.findAll());
        return "admin/appointments";
    }

    @GetMapping("/appointments/{id}/edit")
    public String editAppointment(@PathVariable Long id, Model model) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            model.addAttribute("appointment", appointmentOpt.get());
            model.addAttribute("doctors", doctorRepository.findAll());
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("statuses", AppointmentStatus.values());
            return "admin/appointment-edit";
        }
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/edit")
    public String updateAppointment(@PathVariable Long id, @ModelAttribute Appointment appointment, RedirectAttributes redirectAttributes) {
        try {
            Optional<Appointment> existingAppOpt = appointmentRepository.findById(id);
            if (existingAppOpt.isPresent()) {
                Appointment existingApp = existingAppOpt.get();
                existingApp.setAppointmentDateTime(appointment.getAppointmentDateTime());
                existingApp.setStatus(appointment.getStatus());
                existingApp.setReason(appointment.getReason());
                existingApp.setNotes(appointment.getNotes());
                existingApp.setDiagnosis(appointment.getDiagnosis());
                existingApp.setPrescription(appointment.getPrescription());
                existingApp.setDurationMinutes(appointment.getDurationMinutes());
                existingApp.setConsultationFee(appointment.getConsultationFee());
                
                appointmentRepository.save(existingApp);
                redirectAttributes.addFlashAttribute("success", "Appointment updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating appointment: " + e.getMessage());
        }
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/delete")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Appointment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting appointment: " + e.getMessage());
        }
        return "redirect:/admin/appointments";
    }

    @GetMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);
                redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Appointment not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling appointment: " + e.getMessage());
        }
        return "redirect:/admin/appointments";
    }

    @GetMapping("/appointments/{id}/view")
    public String viewAppointment(@PathVariable Long id, Model model) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            model.addAttribute("appointment", appointmentOpt.get());
            return "admin/appointment-view";
        }
        return "redirect:/admin/appointments";
    }

    // ==================== REPORTS ====================
    
    @GetMapping("/reports")
    public String reports(Model model) {
        // Get comprehensive statistics for reports
        long totalUsers = userService.findAllUsers().size();
        long totalPatients = userService.countUsersByRole(Role.PATIENT);
        long totalDoctors = userService.countUsersByRole(Role.DOCTOR);
        long totalDepartments = departmentRepository.count();
        long totalAppointments = appointmentRepository.count();
        
        long scheduledAppointments = appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED);
        long completedAppointments = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        long cancelledAppointments = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("scheduledAppointments", scheduledAppointments);
        model.addAttribute("completedAppointments", completedAppointments);
        model.addAttribute("cancelledAppointments", cancelledAppointments);
        
        return "admin/reports";
    }

    // ==================== ADDITIONAL VIEW ROUTES ====================
    
    @GetMapping("/users/{id}/view")
    public String viewUser(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "admin/user-view";
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/patients/{id}/view")
    public String viewPatient(@PathVariable Long id, Model model) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            model.addAttribute("patient", patientOpt.get());
            return "admin/patient-view";
        }
        return "redirect:/admin/patients";
    }
    
    @GetMapping("/patients/{id}/medical-history")
    public String patientMedicalHistory(@PathVariable Long id, Model model) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            model.addAttribute("patient", patient);
            // Get patient's appointments for medical history
            model.addAttribute("appointments", appointmentRepository.findByPatientIdOrderByDateDesc(patient.getId()));
            return "admin/patient-medical-history";
        }
        return "redirect:/admin/patients";
    }
    
    @GetMapping("/doctors/{id}/view")
    public String viewDoctor(@PathVariable Long id, Model model) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isPresent()) {
            model.addAttribute("doctor", doctorOpt.get());
            return "admin/doctor-view";
        }
        return "redirect:/admin/doctors";
    }
    
    @GetMapping("/doctors/{id}/schedule")
    public String doctorSchedule(@PathVariable Long id, Model model) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            model.addAttribute("doctor", doctor);
            // Get doctor's appointments for schedule view
            model.addAttribute("appointments", appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId()));
            return "admin/doctor-schedule";
        }
        return "redirect:/admin/doctors";
    }
}
