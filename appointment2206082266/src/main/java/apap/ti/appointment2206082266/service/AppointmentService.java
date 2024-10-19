package apap.ti.appointment2206082266.service;

import java.util.Date;
import java.util.List;
import java.util.Map;


import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;

public interface AppointmentService {
    Appointment createAppointmentWithPatient(AddPatientAppointmentRequestDTO dto);
    String generateAppointmentId(String specCode, Date date);
    List<Appointment> getAllAppointments();
    Appointment getAppointmentById(String id);
    Appointment addAppointment(Appointment appointment);
    Appointment updateAppointment(Appointment appointment);
    Appointment updateDiagnosisTreatment(Appointment appointment);
    Appointment updateStatus(String id, int status);
    void deleteAppointment(Appointment appointment);
    List<Appointment> getAppointmentsByDateRange(Date fromDate, Date toDate);
    long countAppointmentsByDateRange(Date fromDate, Date toDate);
    List<AppointmentResponseDTO> getAllApointmentFromRest() throws Exception;
    List<AppointmentResponseDTO> getAllAppointmentsHybridFromRest(Date fromDate, Date toDate) throws Exception;
    long countAppointmentsHybridFromRest(Date fromDate, Date toDate) throws Exception;
    Map<String, Object> getAppointmentStatisticsFromRest(String period, int year) throws Exception;
}