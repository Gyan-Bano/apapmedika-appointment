package apap.ti.appointment2206082266.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.repository.AppointmentDb;
import apap.ti.appointment2206082266.repository.DoctorDb;
import apap.ti.appointment2206082266.repository.PatientDb;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private PatientDb patientDb;

    @Autowired
    private AppointmentDb appointmentDb;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorDb doctorDb; 

    private static int globalSequenceNumber = 0; 
    private static Date lastGeneratedDate; // Store the last date when an ID was generated

    @Override
    public synchronized String generateAppointmentId(String specCode, Date date) {

        // Format the date as "DDMM"
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
        Patient patient = patientDb.findByNik(dto.getNik());
        if (patient == null) {
            // Create new patient
            patient = new Patient();
            patient.setNik(dto.getNik());
            patient.setName(dto.getName());
            patient.setGender(dto.getGender());
            patient.setEmail(dto.getEmail());
            patient.setBirthDate(dto.getBirthDate());
            patient.setBirthPlace(dto.getBirthPlace());
            patient = patientDb.save(patient);
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctorService.getDoctorById(dto.getDoctorId()));
        appointment.setDiagnosis(dto.getDiagnosis());
        appointment.setDate(dto.getAppointmentDate());
        appointment.setStatus(0); // Default status to pending or unconfirmed

        String specCode = dto.getDoctorId().substring(0, 3);
        String appointmentID = generateAppointmentId(specCode, dto.getAppointmentDate());
        appointment.setId(appointmentID);

        Doctor doctor = doctorService.getDoctorById(dto.getDoctorId());
        doctor.getAppointments().add(appointment);
        doctorDb.save(doctor);
        // Save appointment to DB
        return appointmentDb.save(appointment);
    }
}
