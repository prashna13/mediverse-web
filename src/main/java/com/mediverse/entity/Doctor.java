package com.mediverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "department.doctors"})
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(name = "doctor_id", unique = true)
    private String doctorId;
    
    @Column(name = "specialization", nullable = false)
    @NotBlank(message = "Specialization is required")
    private String specialization;
    
    @Column(name = "license_number", unique = true)
    private String licenseNumber;
    
    @Column(name = "qualification")
    private String qualification;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(name = "consultation_fee")
    private Double consultationFee;
    
    @Column(name = "availability_start_time")
    private LocalTime availabilityStartTime;
    
    @Column(name = "availability_end_time")
    private LocalTime availabilityEndTime;
    
    @Column(name = "working_days")
    private String workingDays; // JSON string or comma-separated values
    
    @Column(name = "bio", length = 2000)
    private String bio;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();
    
    // Constructors
    public Doctor() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Doctor(User user, String specialization) {
        this();
        this.user = user;
        this.specialization = specialization;
        this.doctorId = generateDoctorId();
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.doctorId == null) {
            this.doctorId = generateDoctorId();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    private String generateDoctorId() {
        return "DOC" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public String getQualification() {
        return qualification;
    }
    
    public void setQualification(String qualification) {
        this.qualification = qualification;
    }
    
    public Integer getExperienceYears() {
        return experienceYears;
    }
    
    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }
    
    public Double getConsultationFee() {
        return consultationFee;
    }
    
    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
    }
    
    public LocalTime getAvailabilityStartTime() {
        return availabilityStartTime;
    }
    
    public void setAvailabilityStartTime(LocalTime availabilityStartTime) {
        this.availabilityStartTime = availabilityStartTime;
    }
    
    public LocalTime getAvailabilityEndTime() {
        return availabilityEndTime;
    }
    
    public void setAvailabilityEndTime(LocalTime availabilityEndTime) {
        this.availabilityEndTime = availabilityEndTime;
    }
    
    public String getWorkingDays() {
        return workingDays;
    }
    
    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
    
    // Utility methods
    public String getFullName() {
        return user != null ? user.getFullName() : "";
    }
    
    public String getEmail() {
        return user != null ? user.getEmail() : "";
    }
    
    public String getPhoneNumber() {
        return user != null ? user.getPhoneNumber() : "";
    }
    
    public String getDepartmentName() {
        return department != null ? department.getName() : "";
    }
    
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", doctorId='" + doctorId + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", specialization='" + specialization + '\'' +
                ", department='" + getDepartmentName() + '\'' +
                '}';
    }
}
