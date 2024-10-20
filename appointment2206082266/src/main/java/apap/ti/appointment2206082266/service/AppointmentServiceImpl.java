package apap.ti.appointment2206082266.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.repository.AppointmentDb;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.BaseResponseDTO;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.HashMap;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final PatientService patientService;
    private final AppointmentDb appointmentDb;
    private final DoctorService doctorService;
    private final WebClient webClient;

    public AppointmentServiceImpl(PatientService patientService,
                                  AppointmentDb appointmentDb,
                                  DoctorService doctorService,
                                  WebClient.Builder webClientBuilder) {
        this.patientService = patientService;
        this.appointmentDb = appointmentDb;
        this.doctorService = doctorService;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api").build();
    }


    private Map<String, Integer> dailyAppointmentCount = new HashMap<>();

    @Override
    public synchronized String generateAppointmentId(String specCode, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMM");
        String dateStr = dateFormat.format(date);

        // Increment the global appointment count for the specific day
        int appointmentCount = dailyAppointmentCount.getOrDefault(dateStr, 0) + 1;
        dailyAppointmentCount.put(dateStr, appointmentCount);

        String sequence = String.format("%03d", appointmentCount); // Ensuring it's always three digits

        return specCode + dateStr + sequence; // Combine all parts to form the ID
    }

    @Override
    @Transactional
    public Appointment createAppointmentWithPatient(AddPatientAppointmentRequestDTO dto) {
        // Check if patient exists
        Patient patient = patientService.getPatientByNik(dto.getNik());
        if (patient == null) {
            // Create new patient
            patient = new Patient();
            patient.setNik(dto.getNik());
            patient.setName(dto.getName());
            patient.setGender(dto.getGender());
            patient.setEmail(dto.getEmail());
            patient.setBirthDate(dto.getBirthDate());
            patient.setBirthPlace(dto.getBirthPlace());
            patient = patientService.addPatient(patient);
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctorService.getDoctorById(dto.getDoctorId()));
        appointment.setDate(dto.getAppointmentDate());
        appointment.setStatus(0); // Default status to pending or unconfirmed
        appointment.setTotalFee(doctorService.getDoctorById(dto.getDoctorId()).getFee());

        String specCode = dto.getDoctorId().substring(0, 3);
        String appointmentID = generateAppointmentId(specCode, dto.getAppointmentDate());
        appointment.setId(appointmentID);

        doctorService.addAppointmentToDoctor(dto.getDoctorId(), appointment);
    
        // Save appointment to DB
        return appointmentDb.save(appointment);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentDb.findAll();
    }

    @Override
    public Appointment getAppointmentById(String id) {
        return appointmentDb.findById(id).orElse(null);
    }

    @Override
    public Appointment addAppointment(Appointment appointment) {
        return appointmentDb.save(appointment);
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Appointment appointment) {
        Appointment existingAppointment = appointmentDb.findById(appointment.getId())
            .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        existingAppointment.setPatient(appointment.getPatient());
        existingAppointment.setDate(appointment.getDate());
        existingAppointment.setTotalFee(appointment.getTotalFee());
        existingAppointment.setUpdatedAt(new Date());

        // Update doctor if changed
        if (!existingAppointment.getDoctor().getId().equals(appointment.getDoctor().getId())) {
            doctorService.updateAppointmentDoctor(existingAppointment.getDoctor().getId(), 
                                                  appointment.getDoctor().getId(), 
                                                  existingAppointment);
            existingAppointment.setDoctor(appointment.getDoctor());
        }

        return appointmentDb.save(existingAppointment);
    }

    @Override
    public Appointment updateDiagnosisTreatment(Appointment appointment) {
        Appointment existingAppointment = appointmentDb.findById(appointment.getId())
            .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        existingAppointment.setDiagnosis(appointment.getDiagnosis());
        existingAppointment.setTreatments(appointment.getTreatments());
        existingAppointment.setTotalFee(appointment.getTotalFee());


        return appointmentDb.save(existingAppointment);
    }
    
    @Override
    public Appointment updateStatus(String id, int status) {
        Appointment existingAppointment = appointmentDb.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        existingAppointment.setStatus(status);;

        return appointmentDb.save(existingAppointment);
    }

    @Override
    public void deleteAppointment(Appointment appointment) {
    
        appointment.setDeletedAt(new Date());
        appointmentDb.save(appointment);
    }

    @Override
    public List<Appointment> getAppointmentsByDateRange(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            return appointmentDb.findAllActiveAppointments();
        }
        return appointmentDb.findActiveAppointmentsByDateRange(fromDate, toDate);
    }

    @Override
    public long countAppointmentsByDateRange(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            return appointmentDb.countAllActiveAppointments();
        }
        return appointmentDb.countActiveAppointmentsByDateRange(fromDate, toDate);
    }


    @Override
    public List<AppointmentResponseDTO> getAllAppointmentsFromRest(Date fromDate, Date toDate) throws Exception {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/appointment/all");
        
        if (fromDate != null) {
            uriBuilder.queryParam("fromDate", new SimpleDateFormat("yyyy-MM-dd").format(fromDate));
        }
        if (toDate != null) {
            uriBuilder.queryParam("toDate", new SimpleDateFormat("yyyy-MM-dd").format(toDate));
        }

        String uri = uriBuilder.build().toUriString();

        var response = webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<BaseResponseDTO<List<AppointmentResponseDTO>>>() {})
            .block();

        if (response == null) {
            throw new Exception("Failed to consume API getAllAppointments");
        }

        if (response.getStatus() != 200) {
            throw new Exception(response.getMessage());
        }
        return response.getData();
    }

    @Override
    public long countAppointmentsFromRest(Date fromDate, Date toDate) throws Exception {
        List<AppointmentResponseDTO> appointments = getAllAppointmentsFromRest(fromDate, toDate);
        return appointments.size();
    }

    @Override
    public Map<String, Object> getAppointmentStatisticsFromRest(String period, int year) throws Exception {
        // Build the request with WebClient and pass period and year as query parameters
        var response = webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/appointment/stat")
                .queryParam("period", period)
                .queryParam("year", year)
                .build()
            )
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<BaseResponseDTO<Map<String, Object>>>() {})
            .block(); // Block to wait for the response

        // Handle possible null or error responses
        if (response == null) {
            throw new Exception("Failed to consume API for appointment statistics");
        }

        if (response.getStatus() != 200) {
            throw new Exception(response.getMessage());
        }

        // Return the data (statistics map) from the response
        return response.getData();
    }

    // Service implementation
    public List<Appointment> getTodayActiveStatusZeroCreatedAppointments() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calendar.getTime();
        return appointmentDb.findActiveStatusZeroCreatedAppointmentsForDate(startOfDay, endOfDay);
    }
}
