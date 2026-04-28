package com.mediverse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Department name is required")
    private String name;
    
    @Column(unique = true, nullable = false, length = 10)
    @NotBlank(message = "Department code is required")
    private String code;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "head_doctor")
    private String headDoctor;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Doctor> doctors = new ArrayList<>();
    
    @Transient
    private int doctorCount = 0;
    
    // Constructors
    public Department() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Department(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code != null ? code.toUpperCase() : null;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getHeadDoctor() {
        return headDoctor;
    }
    
    public void setHeadDoctor(String headDoctor) {
        this.headDoctor = headDoctor;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    public List<Doctor> getDoctors() {
        return doctors;
    }
    
    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
    }
    
    // Utility methods
    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
        doctor.setDepartment(this);
    }
    
    public void removeDoctor(Doctor doctor) {
        doctors.remove(doctor);
        doctor.setDepartment(null);
    }
    
    public int getDoctorCount() {
        return doctorCount;
    }
    
    public void setDoctorCount(int doctorCount) {
        this.doctorCount = doctorCount;
    }
    
    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", headDoctor='" + headDoctor + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
