package apap.ti.appointment2206082266.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import apap.ti.appointment2206082266.dto.request.AddAppointmentRequestDTO;
import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.model.SpecializationInfo;
import apap.ti.appointment2206082266.service.AppointmentService;
import apap.ti.appointment2206082266.service.DoctorService;
import apap.ti.appointment2206082266.service.PatientService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    private static final Map<Integer, String> STATUS_MAP;

    static {
        STATUS_MAP = new HashMap<>();
        STATUS_MAP.put(0, "Created");
        STATUS_MAP.put(1, "Done");
        STATUS_MAP.put(2, "Cancelled");
    }
    

    @GetMapping("/all")
    public String viewAllAppointment(Model model) {
        List<Appointment> listAppointments = appointmentService.getAllAppointments();
        model.addAttribute("listAppointments", listAppointments);
        model.addAttribute("statusMap", STATUS_MAP);

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

    @GetMapping("/{id}")
    public String detailAppointment(@PathVariable String id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        Map<String, Object> formattedAppointment = new HashMap<>();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d MMMM yyyy HH:mm");
        
        formattedAppointment.put("id", appointment.getId());
        formattedAppointment.put("patient", appointment.getPatient().getName());
        formattedAppointment.put("doctor", appointment.getDoctor().getName());
        formattedAppointment.put("diagnosis", appointment.getDiagnosis());
        formattedAppointment.put("date", dateFormat.format(appointment.getDate()));
        formattedAppointment.put("status", STATUS_MAP.get(appointment.getStatus()));
        formattedAppointment.put("createdAt", dateTimeFormat.format(appointment.getCreatedAt()));
        formattedAppointment.put("updatedAt", dateTimeFormat.format(appointment.getUpdatedAt()));
        
        model.addAttribute("appointment", formattedAppointment);
        return "view-appointment";
    }

    @GetMapping("/{nik}/create") 
    public String formCreateAppointmentWithExistingPatient(@PathVariable String nik, Model model) {
        Patient patient = patientService.getPatientByNik(nik);
        model.addAttribute("patient", patient);

        AddAppointmentRequestDTO appointmentWithExistingPatientRequestDTO = new AddAppointmentRequestDTO();
        appointmentWithExistingPatientRequestDTO.setNik(nik);
        model.addAttribute("AddAppointmentRequestDTO", appointmentWithExistingPatientRequestDTO);

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

        return "form-add-appointment";
    }

    @PostMapping("/{nik}/create") 
    public String createAppointmentWithExistingPatient(@PathVariable String nik, @Valid @ModelAttribute("AddAppointmentRequestDTO") AddAppointmentRequestDTO appointmentWithExistingPatientRequestDTO,
                                                       Model model) {
        
        Appointment appointment = new Appointment();
        appointment.setPatient(patientService.getPatientByNik(appointmentWithExistingPatientRequestDTO.getNik()));
        appointment.setDoctor(doctorService.getDoctorById(appointmentWithExistingPatientRequestDTO.getDoctorId()));
        appointment.setDiagnosis(appointmentWithExistingPatientRequestDTO.getDiagnosis());
        appointment.setDate(appointmentWithExistingPatientRequestDTO.getAppointmentDate());
        appointment.setStatus(0); // Default status to pending or unconfirmed
        appointment.setTotalFee(doctorService.getDoctorById(appointmentWithExistingPatientRequestDTO.getDoctorId()).getFee());

        String specCode = appointmentWithExistingPatientRequestDTO.getDoctorId().substring(0, 3);
        String appointmentID = appointmentService.generateAppointmentId(specCode, appointmentWithExistingPatientRequestDTO.getAppointmentDate());
        appointment.setId(appointmentID);

        appointmentService.addAppointment(appointment);
        doctorService.addAppointmentToDoctor(appointmentWithExistingPatientRequestDTO.getDoctorId(), appointment);

    
        model.addAttribute("responseMessage", String.format("Appointment untuk pasien dengan ID %s berhasil dibuat dengan ID appointment %s.", appointment.getPatient().getId(), appointment.getId()));

        return "response-appointment";
    }
}

