package com.mediverse.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String timeStr = p.getText().trim();
        if (timeStr.isEmpty()) {
            return null;
        }
        
        try {
            // Try to parse with seconds first
            if (timeStr.matches("^\\d{1,2}:\\d{2}:\\d{2}$")) {
                return LocalTime.parse(timeStr);
            }
            // Then try without seconds
            else if (timeStr.matches("^\\d{1,2}:\\d{2}$")) {
                return LocalTime.parse(timeStr, TIME_FORMATTER);
            }
            // Handle 12-hour format if needed
            else if (timeStr.matches("^\\d{1,2}:\\d{2}\s*[aApP][mM]$")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
                return LocalTime.parse(timeStr.toUpperCase(), formatter);
            }
            
            throw new DateTimeParseException("Invalid time format: " + timeStr + ". Expected format: HH:mm or HH:mm:ss", timeStr, 0);
            
        } catch (DateTimeParseException e) {
            throw new IOException("Failed to parse time: " + timeStr + ". " + e.getMessage(), e);
        }
    }
}
