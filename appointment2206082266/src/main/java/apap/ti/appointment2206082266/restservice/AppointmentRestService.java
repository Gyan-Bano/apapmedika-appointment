package apap.ti.appointment2206082266.restservice;

import java.util.Date; // Import the Date class

import java.util.List;

import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;

public interface AppointmentRestService {
    List<AppointmentResponseDTO> getAllAppointment();
    long countAppointmentsByDateRange(Date startDate, Date endDate);

} 
