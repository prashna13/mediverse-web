package com.mediverse.config;

import com.mediverse.entity.*;
import com.mediverse.repository.*;
import com.mediverse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists to avoid duplicates
        if (userRepository.count() > 0) {
            System.out.println("Data already exists. Skipping initialization.");
            return;
        }

        System.out.println("Initializing sample data for MySQL database...");

        // Create admin user
        User adminUser = new User();
        adminUser.setEmail("admin@mediverse.com");
        adminUser.setPassword("admin123");
        adminUser.setRole(Role.ADMIN);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPhoneNumber("+1-555-0000");
        userService.saveUser(adminUser);

        // Create departments
        Department cardiology = createDepartment("Cardiology", "Heart and cardiovascular system care", 
                "cardiology@mediverse.com", "+1-555-0001", "Dr. Sarah Johnson");
        Department neurology = createDepartment("Neurology", "Brain and nervous system disorders", 
                "neurology@mediverse.com", "+1-555-0002", "Dr. Michael Chen");
        Department orthopedics = createDepartment("Orthopedics", "Bone, joint, and muscle treatment", 
                "orthopedics@mediverse.com", "+1-555-0003", "Dr. Robert Davis");
        Department pediatrics = createDepartment("Pediatrics", "Children's health and development", 
                "pediatrics@mediverse.com", "+1-555-0004", "Dr. Emily Wilson");
        Department emergency = createDepartment("Emergency Medicine", "24/7 emergency and critical care", 
                "emergency@mediverse.com", "+1-555-0005", "Dr. James Martinez");
        Department radiology = createDepartment("Radiology", "Medical imaging and diagnostics", 
                "radiology@mediverse.com", "+1-555-0006", "Dr. Lisa Anderson");

        // Create branches
        createBranch("Mediverse Downtown", "123 Main St, Downtown City, ST 12345", 
                "downtown@mediverse.com", "+1-555-1000", "24/7");
        createBranch("Mediverse Uptown", "456 Oak Ave, Uptown City, ST 12346", 
                "uptown@mediverse.com", "+1-555-1001", "Mon-Fri: 8AM-8PM, Sat-Sun: 9AM-5PM");
        createBranch("Mediverse Suburbs", "789 Pine Rd, Suburban City, ST 12347", 
                "suburbs@mediverse.com", "+1-555-1002", "Mon-Fri: 7AM-9PM, Sat: 8AM-6PM, Sun: 10AM-4PM");

        // Create sample doctors
        createSampleDoctor("Sarah", "Johnson", "sarah.johnson@mediverse.com", "Interventional Cardiology", "MD, FACC", 15, 250.0, cardiology);
        createSampleDoctor("Michael", "Chen", "michael.chen@mediverse.com", "Neurosurgery", "MD, PhD", 12, 300.0, neurology);
        createSampleDoctor("Robert", "Davis", "robert.davis@mediverse.com", "Sports Medicine", "MD, FACP", 10, 200.0, orthopedics);
        createSampleDoctor("Emily", "Wilson", "emily.wilson@mediverse.com", "Pediatric Oncology", "MD, MPH", 8, 180.0, pediatrics);
        createSampleDoctor("James", "Martinez", "james.martinez@mediverse.com", "Emergency Medicine", "MD, FACEP", 14, 220.0, emergency);
        createSampleDoctor("Lisa", "Anderson", "lisa.anderson@mediverse.com", "Diagnostic Radiology", "MD, FACR", 11, 190.0, radiology);

        // Create sample patients
        createSamplePatient("John", "Doe", "john.doe@email.com", "+1-555-2001");
        createSamplePatient("Jane", "Smith", "jane.smith@email.com", "+1-555-2002");
        createSamplePatient("Bob", "Brown", "bob.brown@email.com", "+1-555-2003");
        createSamplePatient("Alice", "Green", "alice.green@email.com", "+1-555-2004");

        System.out.println("Sample data initialization completed successfully for MySQL!");
    }

    private Department createDepartment(String name, String description, String email, String phone, String headDoctor) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setEmail(email);
        department.setPhoneNumber(phone);
        department.setHeadDoctor(headDoctor);
        return departmentRepository.save(department);
    }

    private void createBranch(String name, String address, String email, String phone, String hours) {
        Branch branch = new Branch();
        branch.setName(name);
        branch.setAddress(address);
        branch.setEmail(email);
        branch.setPhoneNumber(phone);
        branch.setOperatingHours(hours);
        branchRepository.save(branch);
    }

    private void createSampleDoctor(String firstName, String lastName, String email, 
                                   String specialization, String qualification, int experience, double fee, Department department) {
        User doctorUser = new User();
        doctorUser.setEmail(email);
        doctorUser.setPassword("doctor123");
        doctorUser.setRole(Role.DOCTOR);
        doctorUser.setFirstName(firstName);
        doctorUser.setLastName(lastName);
        doctorUser.setPhoneNumber("+1-555-" + (int)(Math.random() * 9000 + 1000));
        User savedDoctorUser = userService.saveUser(doctorUser);

        Doctor doctor = new Doctor();
        doctor.setUser(savedDoctorUser);
        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setExperienceYears(experience);
        doctor.setConsultationFee(fee);
        doctor.setAvailabilityStartTime(LocalTime.of(9, 0));
        doctor.setAvailabilityEndTime(LocalTime.of(17, 0));
        doctor.setWorkingDays("Monday,Tuesday,Wednesday,Thursday,Friday");
        doctor.setBio("Experienced " + specialization + " specialist with " + experience + " years of practice.");
        doctor.setDepartment(department);
        doctor.setLicenseNumber("LIC" + System.currentTimeMillis());
        doctor.setDoctorId("DOC" + System.currentTimeMillis());
        doctorRepository.save(doctor);
    }

    private void createSamplePatient(String firstName, String lastName, String email, String phone) {
        User patientUser = new User();
        patientUser.setEmail(email);
        patientUser.setPassword("patient123");
        patientUser.setRole(Role.PATIENT);
        patientUser.setFirstName(firstName);
        patientUser.setLastName(lastName);
        patientUser.setPhoneNumber(phone);
        User savedPatientUser = userService.saveUser(patientUser);

        Patient patient = new Patient();
        patient.setUser(savedPatientUser);
        patient.setGender(Gender.MALE);
        patient.setBloodGroup("O+");
        patient.setAddress("123 Sample St, City, ST 12345");
        patient.setEmergencyContactName("Emergency Contact");
        patient.setEmergencyContactPhone("+1-555-9999");
        patient.setPatientId("PAT" + System.currentTimeMillis());
        patientRepository.save(patient);
    }
}
