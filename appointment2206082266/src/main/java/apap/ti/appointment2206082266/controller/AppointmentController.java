package apap.ti.appointment2206082266.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import apap.ti.appointment2206082266.dto.request.AddAppointmentRequestDTO;
import apap.ti.appointment2206082266.dto.request.AddPatientAppointmentRequestDTO;
import apap.ti.appointment2206082266.dto.request.UpdateDiagnosisTreatmentRequestDTO;
import apap.ti.appointment2206082266.dto.request.UpdateAppointmentRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.model.SpecializationInfo;
import apap.ti.appointment2206082266.model.Treatment;
import apap.ti.appointment2206082266.service.AppointmentService;
import apap.ti.appointment2206082266.service.DoctorService;
import apap.ti.appointment2206082266.service.PatientService;
import apap.ti.appointment2206082266.service.TreatmentService;
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

    @Autowired
    private TreatmentService treatmentService;

    private static final Map<Integer, String> STATUS_MAP;

    static {
        STATUS_MAP = new HashMap<>();
        STATUS_MAP.put(0, "Created");
        STATUS_MAP.put(1, "Done");
        STATUS_MAP.put(2, "Cancelled");
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d MMMM yyyy HH:mm");

    // Refactored helper method to format appointment data
    private Map<String, Object> formatAppointment(Appointment appointment) {
        Map<String, Object> formattedAppointment = new HashMap<>();
        
        formattedAppointment.put("id", appointment.getId());
        formattedAppointment.put("patient", appointment.getPatient().getName());
        formattedAppointment.put("doctor", appointment.getDoctor().getName());
        formattedAppointment.put("diagnosis", appointment.getDiagnosis());
        formattedAppointment.put("date", dateFormat.format(appointment.getDate()));
        formattedAppointment.put("treatments", appointment.getTreatments());
        formattedAppointment.put("status", STATUS_MAP.get(appointment.getStatus()));
        formattedAppointment.put("createdAt", dateTimeFormat.format(appointment.getCreatedAt()));
        formattedAppointment.put("updatedAt", dateTimeFormat.format(appointment.getUpdatedAt()));

        return formattedAppointment;
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
        if (patientService.getPatientByNik(appointmentWithNewPatientRequestDTO.getNik()) == null) {
            Appointment savedAppointment = appointmentService.createAppointmentWithPatient(appointmentWithNewPatientRequestDTO);

            // Add a success message to the model
            model.addAttribute("responseMessage", String.format("Pasien %s dengan ID %s berhasil ditambahkan. Appointment untuk pasien ini juga berhasil dibuat dengan ID appointment %s.", savedAppointment.getPatient().getName(), savedAppointment.getPatient().getId(), savedAppointment.getId()));
            return "response-appointment";
        } else {
            // Add an error message to the model
            model.addAttribute("responseMessage", String.format("Pasien dengan NIK %s sudah ada. Appointment gagal dibuat.", appointmentWithNewPatientRequestDTO.getNik()));

            return formCreateAppointmentWithPatient(model);
        }
    }

    @GetMapping("/{id}")
    public String detailAppointment(@PathVariable String id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);

        // Add attributes to the model
        model.addAttribute("appointment", formatAppointment(appointment));
        model.addAttribute("editable", false);

        return "view-appointment"; // Points to the Thymeleaf template
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

    @GetMapping("/{id}/update")
    public String updateAppointmentForm(@PathVariable String id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);

        var appointmentRequestDTO = new UpdateAppointmentRequestDTO();
        appointmentRequestDTO.setId(appointment.getId());
        appointmentRequestDTO.setDoctorId(appointment.getDoctor().getId());
        appointmentRequestDTO.setAppointmentDate(appointment.getDate());

        model.addAttribute("UpdateAppointmentRequestDTO", appointmentRequestDTO);

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
        // Log the data being sent to the view
        System.out.println("Appointment Treatments: " + appointment.getTreatments().toString());
        System.out.println("Available Treatments: " + treatmentService.getAllTreatments().toString());
        model.addAttribute("doctorSchedules", doctorSchedules); // Send schedules to the view

        return "form-update-appointment";
    }

    @PostMapping("/update")
    public String updateAppointment(@Valid @ModelAttribute("UpdateAppointmentRequestDTO") UpdateAppointmentRequestDTO appointmentRequestDTO, Model model) {
        Appointment appointmentFromDTO = appointmentService.getAppointmentById(appointmentRequestDTO.getId());
        appointmentFromDTO.setId(appointmentRequestDTO.getId());
        appointmentFromDTO.setDoctor(doctorService.getDoctorById(appointmentRequestDTO.getDoctorId()));
        appointmentFromDTO.setDate(appointmentRequestDTO.getAppointmentDate());

        var appointment = appointmentService.updateAppointment(appointmentFromDTO);

        model.addAttribute("responseMessage", 
            String.format("Appointment dengan ID %s berhasil diupdate.", appointment.getId()));
        return "response-appointment";                     
    }

    
    @GetMapping("/{id}/note")
    public String viewAppointmentNoteForm(@PathVariable String id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        var diagnosisTreatmentDTO = new UpdateDiagnosisTreatmentRequestDTO();
        diagnosisTreatmentDTO.setId(appointment.getId());
        diagnosisTreatmentDTO.setDiagnosis(appointment.getDiagnosis());

        List<Long> treatmentIds;
        if (appointment.getTreatments() != null) {
            treatmentIds = appointment.getTreatments().stream()
                .map(Treatment::getId)
                .collect(Collectors.toList());
        } else {
            treatmentIds = new ArrayList<>();
        }

        diagnosisTreatmentDTO.setTreatmentIds(treatmentIds);
        
        model.addAttribute("UpdateDiagnosisTreatmentRequestDTO", diagnosisTreatmentDTO);
        model.addAttribute("appointment", formatAppointment(appointment));
        model.addAttribute("editable", true);
        model.addAttribute("treatments", treatmentService.getAllTreatments());
    
        return "view-appointment";
    }
    
    @PostMapping("/note")
    public String updateAppointmentNoteForm(
            @ModelAttribute("UpdateDiagnosisTreatmentRequestDTO") UpdateDiagnosisTreatmentRequestDTO diagnosisTreatmentDTO,
            @RequestParam(value = "addRow", required = false) String addRow,
            @RequestParam(value = "deleteRow", required = false) Integer deleteRow,
            Model model) {

        if (diagnosisTreatmentDTO.getTreatmentIds() == null) {
            diagnosisTreatmentDTO.setTreatmentIds(new ArrayList<>());
        }
        model.addAttribute("appointment", formatAppointment(appointmentService.getAppointmentById(diagnosisTreatmentDTO.getId())));

        if (addRow != null) {
            // Add a new empty treatment
            diagnosisTreatmentDTO.getTreatmentIds().add(null);
        } else if (deleteRow != null) {
            // Remove the treatment at the specified index
            diagnosisTreatmentDTO.getTreatmentIds().remove(deleteRow.intValue());
        } else {
            // Save the form
            Appointment appointment = appointmentService.getAppointmentById(diagnosisTreatmentDTO.getId());
            appointment.setDiagnosis(diagnosisTreatmentDTO.getDiagnosis());
            
            // Convert treatment IDs back to Treatment objects
            List<Treatment> treatments = diagnosisTreatmentDTO.getTreatmentIds().stream()
                .filter(Objects::nonNull)
                .map(treatmentService::getTreatmentById)
                .collect(Collectors.toList());
            appointment.setTreatments(treatments);

            appointmentService.updateDiagnosisTreatment(appointment);

            model.addAttribute("responseMessage",
                String.format("Berhasil mencatat diagnosis & treatment untuk appointment ID %s.", appointment.getId()));
            return "response-appointment";
        }

        // If we're adding or deleting a row, re-render the form
        model.addAttribute("UpdateDiagnosisTreatmentRequestDTO", diagnosisTreatmentDTO);
        model.addAttribute("editable", true);
        model.addAttribute("treatments", treatmentService.getAllTreatments());
        return "view-appointment";
    }    
}


