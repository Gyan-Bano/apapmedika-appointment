package apap.ti.appointment2206082266.restservice;

import java.util.Date; // Import the Date class

import java.util.List;

import apap.ti.appointment2206082266.restdto.request.AddAppointmentRequestRestDTO;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;

public interface AppointmentRestService {
    List<AppointmentResponseDTO> getAllAppointments(Date fromDate, Date toDate);
    long countAppointmentsByDateRange(Date startDate, Date endDate);
    AppointmentResponseDTO getAppointmentById(String id);
    AppointmentResponseDTO addAppointment(String nik, AddAppointmentRequestRestDTO appointment);
}