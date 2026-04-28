package com.mediverse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "appointment_id", unique = true)
    private String appointmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor is required")
    private Doctor doctor;
    
    @Column(name = "appointment_date_time", nullable = false)
    @NotNull(message = "Appointment date and time is required")
    private LocalDateTime appointmentDateTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "notes", length = 2000)
    private String notes;
    
    @Column(name = "prescription", length = 2000)
    private String prescription;
    
    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30; // Default 30 minutes
    
    @Column(name = "consultation_fee")
    private Double consultationFee;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Appointment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Appointment(Patient patient, Doctor doctor, LocalDateTime appointmentDateTime) {
        this();
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentId = generateAppointmentId();
        this.consultationFee = doctor.getConsultationFee();
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.appointmentId == null) {
            this.appointmentId = generateAppointmentId();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    private String generateAppointmentId() {
        return "APP" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public Doctor getDoctor() {
        return doctor;
    }
    
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
    
    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }
    
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPrescription() {
        return prescription;
    }
    
    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public Double getConsultationFee() {
        return consultationFee;
    }
    
    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
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
    
    // Utility methods
    public String getPatientName() {
        return patient != null ? patient.getFullName() : "";
    }
    
    public String getDoctorName() {
        return doctor != null ? doctor.getFullName() : "";
    }
    
    public String getDoctorSpecialization() {
        return doctor != null ? doctor.getSpecialization() : "";
    }
    
    public LocalDateTime getEndDateTime() {
        if (appointmentDateTime != null && durationMinutes != null) {
            return appointmentDateTime.plusMinutes(durationMinutes);
        }
        return appointmentDateTime;
    }
    
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", appointmentId='" + appointmentId + '\'' +
                ", patientName='" + getPatientName() + '\'' +
                ", doctorName='" + getDoctorName() + '\'' +
                ", appointmentDateTime=" + appointmentDateTime +
                ", status=" + status +
                '}';
    }
}
