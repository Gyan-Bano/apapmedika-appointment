package apap.ti.appointment2206082266.restservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.repository.AppointmentDb;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.DoctorResponseDTO;
import apap.ti.appointment2206082266.restdto.response.PatientResponseDTO;
import apap.ti.appointment2206082266.restdto.response.TreatmentResponseDTO;

@Service
@Transactional
public class AppointmentRestServiceImpl implements AppointmentRestService {

    @Autowired
    private AppointmentDb appointmentDb;
    
    @Override
    public List<AppointmentResponseDTO> getAllAppointment() {
        List<Appointment> appointmentList = appointmentDb.findAll();
        return appointmentList.stream().map(this::appointmentToAppointmentResponseDTO).collect(Collectors.toList());
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
}
