package com.mediverse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mediverse.util.LocalTimeDeserializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentBookingDTO {
    
    @NotNull(message = "Doctor selection is required")
    private Long doctorId;
    
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Appointment time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime appointmentTime;
    
    @NotBlank(message = "Reason for visit is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
    
    @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
    private String notes;
    
    private Long departmentId; // Optional, for filtering doctors
    
    // Constructors
    public AppointmentBookingDTO() {}
    
    public AppointmentBookingDTO(Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime, String reason) {
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
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
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
