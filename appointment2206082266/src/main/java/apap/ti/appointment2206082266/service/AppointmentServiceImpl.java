package apap.ti.appointment2206082266.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.repository.AppointmentDb;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.BaseResponseDTO;
import apap.ti.appointment2206082266.restservice.AppointmentRestService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.reactive.function.client.WebClient;

import java.text.SimpleDateFormat;
import java.util.HashMap;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentDb appointmentDb;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentRestService appointmentRestService;

    private final WebClient webClient;

    public AppointmentServiceImpl(WebClient.Builder webClientBuilder) {
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
    public List<AppointmentResponseDTO> getAllApointmentFromRest() throws Exception {
        var response = webClient
        .get()
        .uri("/appointment/all")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<BaseResponseDTO<List<AppointmentResponseDTO>>>() {})
        .block();
        if (response == null) {
            throw new Exception("Failed consume API getAllAppointment");
        }

        if (response.getStatus() != 200) {
            throw new Exception(response.getMessage());
        }
        return response.getData();
    }

    @Override
    public List<AppointmentResponseDTO> getAllAppointmentsHybrid(Date fromDate, Date toDate) throws Exception {
        List<AppointmentResponseDTO> allAppointments = appointmentRestService.getAllAppointment();
        
        if (fromDate == null || toDate == null) {
            return allAppointments;
        }

        return allAppointments.stream()
            .filter(app -> {
                Date appDate = app.getDate();
                return !appDate.before(fromDate) && !appDate.after(toDate);
            })
            .collect(Collectors.toList());
    }

    @Override
    public long countAppointmentsHybrid(Date fromDate, Date toDate) throws Exception {
        List<AppointmentResponseDTO> filteredAppointments = getAllAppointmentsHybrid(fromDate, toDate);
        return filteredAppointments.size();
    }
}
