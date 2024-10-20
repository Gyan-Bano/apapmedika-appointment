package apap.ti.appointment2206082266.service;

import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.restcontroller.AppointmentRestController;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.DoctorResponseDTO;
import apap.ti.appointment2206082266.restdto.response.PatientResponseDTO;
import apap.ti.appointment2206082266.restdto.response.TreatmentResponseDTO;
import apap.ti.appointment2206082266.restservice.AppointmentRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AppointmentRestServiceTest {

    @Mock
    private AppointmentRestService appointmentRestService;

    @InjectMocks
    private AppointmentRestController appointmentRestController;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // Initialize the mocks before each test
        MockitoAnnotations.openMocks(this);

        // Mock Doctor
        doctor = new Doctor();
        doctor.setId("doctor-1");
        doctor.setName("Dr. Smith");
        doctor.setSpecialist(1);
        doctor.setGender(true);
        doctor.setFee(500000L);

        // Mock Patient
        patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setName("John Doe");
        patient.setNik("123456789");
        patient.setGender(true);
        patient.setBirthDate(new Date());
    }

    @Test
    void testGetAllAppointmentsInDateRange() throws Exception {
        // Prepare the input date range
        Date fromDate = dateFormat.parse("2024-10-01");
        Date toDate = dateFormat.parse("2024-10-15");

        // Mock Treatments
        TreatmentResponseDTO treatmentDTO = new TreatmentResponseDTO(1L, "Consultation", 150000L, new Date(), new Date());

        // Mock DoctorResponseDTO
        DoctorResponseDTO doctorResponseDTO = new DoctorResponseDTO();
        doctorResponseDTO.setId(doctor.getId());
        doctorResponseDTO.setName(doctor.getName());
        doctorResponseDTO.setSpecialist(doctor.getSpecialist());
        doctorResponseDTO.setFee(doctor.getFee());

        // Mock PatientResponseDTO
        PatientResponseDTO patientResponseDTO = new PatientResponseDTO();
        patientResponseDTO.setId(patient.getId());
        patientResponseDTO.setName(patient.getName());
        patientResponseDTO.setNik(patient.getNik());

        // Mock Appointment 1
        AppointmentResponseDTO responseDTO1 = new AppointmentResponseDTO();
        responseDTO1.setId("appointment-1");
        responseDTO1.setDoctor(doctorResponseDTO);
        responseDTO1.setPatient(patientResponseDTO);
        responseDTO1.setDate(dateFormat.parse("2024-10-05"));
        responseDTO1.setDiagnosis("Fever");
        responseDTO1.setTreatments(List.of(treatmentDTO));
        responseDTO1.setTotalFee(650000L);
        responseDTO1.setStatus(0); // Created
        responseDTO1.setCreatedAt(new Date());
        responseDTO1.setUpdatedAt(new Date());

        // Mock Appointment 2
        AppointmentResponseDTO responseDTO2 = new AppointmentResponseDTO();
        responseDTO2.setId("appointment-2");
        responseDTO2.setDoctor(doctorResponseDTO);
        responseDTO2.setPatient(patientResponseDTO);
        responseDTO2.setDate(dateFormat.parse("2024-10-10"));
        responseDTO2.setDiagnosis("Cold");
        responseDTO2.setTreatments(List.of(treatmentDTO));
        responseDTO2.setTotalFee(650000L);
        responseDTO2.setStatus(1); // Done
        responseDTO2.setCreatedAt(new Date());
        responseDTO2.setUpdatedAt(new Date());

        // Create a list of mock appointments
        List<AppointmentResponseDTO> mockAppointments = List.of(responseDTO1, responseDTO2);

        // Mock the service behavior
        when(appointmentRestService.getAllAppointments(fromDate, toDate)).thenReturn(mockAppointments);

        // Call the method under test
        var response = appointmentRestController.getAllAppointments(fromDate, toDate);

        // Verify that the service method was called
        verify(appointmentRestService, times(1)).getAllAppointments(fromDate, toDate);

        // Assert that the response contains the correct data
        assertEquals(200, response.getStatusCodeValue()); // Check if the response status is OK (200)
        assertEquals(2, response.getBody().getData().size()); // Check if the correct number of appointments is returned
        assertEquals("John Doe", response.getBody().getData().get(0).getPatient().getName()); // Check first appointment patient name
        assertEquals("Dr. Smith", response.getBody().getData().get(0).getDoctor().getName()); // Check first appointment doctor name
    }

    @Test
    void testGetAllAppointmentsInDateRange_EmptyResult() throws Exception {
        // Prepare the input date range
        Date fromDate = dateFormat.parse("2024-01-01");
        Date toDate = dateFormat.parse("2024-01-15");

        // Mock the service to return an empty list
        when(appointmentRestService.getAllAppointments(fromDate, toDate)).thenReturn(List.of());

        // Call the method under test
        var response = appointmentRestController.getAllAppointments(fromDate, toDate);

        // Verify that the service method was called
        verify(appointmentRestService, times(1)).getAllAppointments(fromDate, toDate);

        // Assert that the response contains the correct data
        assertEquals(200, response.getStatusCodeValue()); // Check if the response status is OK (200)
        assertEquals(0, response.getBody().getData().size()); // Check that no appointments are returned
    }
}
