package apap.ti.appointment2206082266.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.model.SpecializationInfo;
import apap.ti.appointment2206082266.repository.AppointmentDb;
import apap.ti.appointment2206082266.repository.PatientDb;
import apap.ti.appointment2206082266.service.AppointmentService;
import apap.ti.appointment2206082266.service.DoctorService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.transaction.annotation.Transactional;


@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;
;

    @Autowired
    private PatientDb patientDb;

    @Autowired
    private AppointmentDb appointmentDb;

    @GetMapping("/all")
    public String viewAllAppointment(Model model) {
        return "viewall-appointments";
    }

    @GetMapping("/create-with-patient")
    public String formCreateAppointmentWithPatient(Model model) {
        AddPatientAppointmentRequestDTO appointmentWithNewPatientRequestDTO = new AddPatientAppointmentRequestDTO();
        model.addAttribute("AddPatientAppointmentRequestDTO", appointmentWithNewPatientRequestDTO);

        // Load list of doctors
        List<Doctor> listDoctors = doctorService.getAllDoctors();
        model.addAttribute("listDoctors", listDoctors);

        // Load specializations for doctors
        Map<String, String> doctorSpecializations = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            SpecializationInfo specializationInfo = doctorService.getSpecializationCodes().get(doctor.getSpecialist());
            doctorSpecializations.put(doctor.getId(), specializationInfo.getDescription());
        }
        model.addAttribute("doctorSpecializations", doctorSpecializations);

        // Load schedules for all doctors
        Map<String, List<Date>> doctorSchedules = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            List<Date> nextFourWeeks = doctorService.getNextFourWeeks(doctor);
            doctorSchedules.put(doctor.getId(), nextFourWeeks); // Doctor's ID as the key
        }
        model.addAttribute("doctorSchedules", doctorSchedules); // Send schedules to the view

        return "form-add-patient-appointment"; 
    }

    
    @PostMapping("/create-with-patient")
    public String createAppointmentWithPatient(@Valid @ModelAttribute("AddPatientAppointmentRequestDTO") AddPatientAppointmentRequestDTO appointmentWithNewPatientRequestDTO,
                                               Model model) {
        Appointment savedAppointment = appointmentService.createAppointmentWithPatient(appointmentWithNewPatientRequestDTO);

        // Add a success message to the model
        model.addAttribute("responseMessage", String.format("Pasien %s dengan ID %s berhasil ditambahkan. Appointment untuk pasien ini juga berhasil dibuat dengan ID appointment %s.", savedAppointment.getPatient().getName(), savedAppointment.getPatient().getId(), savedAppointment.getId()));

        return "response-appointment";
    }
}
