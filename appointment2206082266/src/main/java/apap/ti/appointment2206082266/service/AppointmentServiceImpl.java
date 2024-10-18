package apap.ti.appointment2206082266.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.repository.AppointmentDb;
import jakarta.persistence.EntityNotFoundException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentDb appointmentDb;

    @Autowired
    private DoctorService doctorService;


    private static int globalSequenceNumber = 0; 
    private static Date lastGeneratedDate;

    @Override
    public synchronized String generateAppointmentId(String specCode, Date date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMM");
        String dateStr = dateFormat.format(date);

        // Reset global sequence number if the date has changed
        if (lastGeneratedDate == null || !isSameDay(lastGeneratedDate, date)) {
            globalSequenceNumber = 0; // Reset for a new day
            lastGeneratedDate = date; // Update the last generated date
        }

        // Increment the global sequence number for the day
        globalSequenceNumber++;
        String sequence = String.format("%03d", globalSequenceNumber % 1000); // Ensuring it's always three digits

        return specCode + dateStr + sequence; // Combine all parts to form the ID
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && 
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
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

        return appointmentDb.save(existingAppointment);
    }
    
}
