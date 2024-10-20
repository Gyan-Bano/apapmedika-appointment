package apap.ti.appointment2206082266.restservice;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.repository.AppointmentDb;
import apap.ti.appointment2206082266.repository.DoctorDb;
import apap.ti.appointment2206082266.repository.PatientDb;
import apap.ti.appointment2206082266.restdto.request.AddAppointmentRequestRestDTO;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.DoctorResponseDTO;
import apap.ti.appointment2206082266.restdto.response.PatientResponseDTO;
import apap.ti.appointment2206082266.restdto.response.TreatmentResponseDTO;
import apap.ti.appointment2206082266.service.AppointmentService;

@Service
@Transactional
public class AppointmentRestServiceImpl implements AppointmentRestService {

    @Autowired
    private AppointmentDb appointmentDb;

    @Autowired
    private DoctorDb doctorDb;

    @Autowired
    private PatientDb patientDb;

    @Autowired
    private AppointmentService appointmentService;
    
    @Override
    public List<AppointmentResponseDTO> getAllAppointments(Date fromDate, Date toDate) {
        List<Appointment> appointmentList;
        
        if (fromDate != null && toDate != null) {
            appointmentList = appointmentDb.findActiveAppointmentsByDateRange(fromDate, toDate);
        } else {
            appointmentList = appointmentDb.findAllActiveAppointments();
        }
        
        return appointmentList.stream()
                .map(this::appointmentToAppointmentResponseDTO)
                .collect(Collectors.toList());
    }

    private AppointmentResponseDTO appointmentToAppointmentResponseDTO(Appointment appointment) {
        AppointmentResponseDTO appointmentResponseDTO = new AppointmentResponseDTO();
        appointmentResponseDTO.setId(appointment.getId());
        appointmentResponseDTO.setDate(appointment.getDate());
        appointmentResponseDTO.setDiagnosis(appointment.getDiagnosis());
        appointmentResponseDTO.setTotalFee(appointment.getTotalFee());
        appointmentResponseDTO.setStatus(appointment.getStatus());
        appointmentResponseDTO.setCreatedAt(appointment.getCreatedAt());
        appointmentResponseDTO.setUpdatedAt(appointment.getUpdatedAt());

        // Convert Patient
        if (appointment.getPatient() != null) {
            PatientResponseDTO patientDTO = new PatientResponseDTO();
            patientDTO.setId(appointment.getPatient().getId());
            patientDTO.setNik(appointment.getPatient().getNik());
            patientDTO.setName(appointment.getPatient().getName());
            patientDTO.setGender(appointment.getPatient().getGender());
            patientDTO.setEmail(appointment.getPatient().getEmail());
            patientDTO.setBirthDate(appointment.getPatient().getBirthDate());
            patientDTO.setBirthPlace(appointment.getPatient().getBirthPlace());
            patientDTO.setCreatedAt(appointment.getPatient().getCreatedAt());
            patientDTO.setUpdatedAt(appointment.getPatient().getUpdatedAt());
            appointmentResponseDTO.setPatient(patientDTO);
        }

        // Convert Doctor
        if (appointment.getDoctor() != null) {
            DoctorResponseDTO doctorDTO = new DoctorResponseDTO();
            doctorDTO.setId(appointment.getDoctor().getId());
            doctorDTO.setName(appointment.getDoctor().getName());
            doctorDTO.setSpecialist(appointment.getDoctor().getSpecialist());
            doctorDTO.setEmail(appointment.getDoctor().getEmail());
            doctorDTO.setGender(appointment.getDoctor().getGender());
            doctorDTO.setYearsOfExperience(appointment.getDoctor().getYearsOfExperience());
            doctorDTO.setSchedules(appointment.getDoctor().getSchedules());
            doctorDTO.setFee(appointment.getDoctor().getFee());
            doctorDTO.setCreatedAt(appointment.getDoctor().getCreatedAt());
            doctorDTO.setUpdatedAt(appointment.getDoctor().getUpdatedAt());
            appointmentResponseDTO.setDoctor(doctorDTO);
        }

        // Convert Treatments
        if (appointment.getTreatments() != null && !appointment.getTreatments().isEmpty()) {
            List<TreatmentResponseDTO> treatmentDTOs = appointment.getTreatments().stream()
                .map(treatment -> {
                    TreatmentResponseDTO treatmentDTO = new TreatmentResponseDTO();
                    treatmentDTO.setId(treatment.getId());
                    treatmentDTO.setName(treatment.getName());
                    treatmentDTO.setPrice(treatment.getPrice());
                    treatmentDTO.setCreatedAt(treatment.getCreatedAt());
                    treatmentDTO.setUpdatedAt(treatment.getUpdatedAt());
                    return treatmentDTO;
                })
                .collect(Collectors.toList());
            appointmentResponseDTO.setTreatments(treatmentDTOs);
        }

        return appointmentResponseDTO;
    }

    @Override
    public long countAppointmentsByDateRange(Date startDate, Date endDate) {
        return appointmentDb.countAllAppointmentsByDateRange(startDate, endDate);
    }

    @Override
    public AppointmentResponseDTO getAppointmentById(String id) {
        Appointment appointment = appointmentDb.findById(id).orElse(null);
        if (appointment == null) {
            return null;
        }
        return appointmentToAppointmentResponseDTO(appointment);
    }

    @Override 
    public AppointmentResponseDTO addAppointment(String nik, AddAppointmentRequestRestDTO appointmentDTO) {
        // Find the patient by NIK
        Patient patient = patientDb.findByNik(nik);
        if (patient == null) {
            throw new RuntimeException("Patient with NIK " + nik + " not found");
        }

        // Find the doctor by ID
        Doctor doctor = doctorDb.findById(appointmentDTO.getDoctorId()).orElse(null);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID " + appointmentDTO.getDoctorId() + " not found");
        }

        // Create new Appointment
        Appointment appointment = new Appointment();
        appointment.setId(appointmentService.generateAppointmentId(appointmentDTO.getDoctorId().substring(0, 3), appointmentDTO.getAppointmentDate()));
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(appointmentDTO.getAppointmentDate());
        appointment.setStatus(0); // Assuming 0 is the initial status for a new appointment
        appointment.setCreatedAt(new Date());
        appointment.setUpdatedAt(new Date());

        // Save the appointment
        Appointment savedAppointment = appointmentDb.save(appointment);

        // Convert and return the saved appointment as DTO
        return appointmentToAppointmentResponseDTO(savedAppointment);
    }
}
