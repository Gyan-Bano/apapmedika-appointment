package apap.ti.appointment2206082266.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
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
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.service.AppointmentService;
import apap.ti.appointment2206082266.service.DoctorService;
import apap.ti.appointment2206082266.service.PatientService;
import apap.ti.appointment2206082266.service.TreatmentService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.LocalDate;
import java.util.Calendar;


@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final TreatmentService treatmentService;
    
    public AppointmentController(DoctorService doctorService,
    AppointmentService appointmentService,
    PatientService patientService,
    TreatmentService treatmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.treatmentService = treatmentService;
    }

    private static final Map<Integer, String> STATUS_MAP;

    static {
        STATUS_MAP = new HashMap<>();
        STATUS_MAP.put(0, "Created");
        STATUS_MAP.put(1, "Done");
        STATUS_MAP.put(2, "Cancelled");
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d MMMM yyyy HH:mm");

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
        formattedAppointment.put("totalFee", appointment.getTotalFee());

        return formattedAppointment;
    }
    
    // @GetMapping("/all")
    // public String viewAllAppointment(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
    //         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate, Model model) {
    //     List<Appointment> filteredAppointments = appointmentService.getAppointmentsByDateRange(fromDate, toDate);
    //     long appointmentCount = appointmentService.countAppointmentsByDateRange(fromDate, toDate);
    //     model.addAttribute("listAppointments", filteredAppointments);
    //     model.addAttribute("appointmentCount", appointmentCount);
    //     model.addAttribute("statusMap", STATUS_MAP);

    //     model.addAttribute("fromDate", fromDate);
    //     model.addAttribute("toDate", toDate);

    //     return "viewall-appointments";
    // }

    @GetMapping("/all")
    public String viewAllAppointment(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate, 
            Model model) {
        try {
            model.addAttribute("page", "appointments");
            List<AppointmentResponseDTO> filteredAppointments = appointmentService.getAllAppointmentsFromRest(fromDate, toDate);
            long appointmentCount = appointmentService.countAppointmentsFromRest(fromDate, toDate);
            
            model.addAttribute("listAppointments", filteredAppointments);
            model.addAttribute("appointmentCount", appointmentCount);
            model.addAttribute("statusMap", STATUS_MAP);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            
            return "viewall-appointments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "response-error-rest";
        }
    }

    @GetMapping("/create-with-patient")
    public String formCreateAppointmentWithPatient(Model model) {
        model.addAttribute("page", "appointments");
        AddPatientAppointmentRequestDTO appointmentWithNewPatientRequestDTO = new AddPatientAppointmentRequestDTO();
        model.addAttribute("AddPatientAppointmentRequestDTO", appointmentWithNewPatientRequestDTO);

        List<Doctor> listDoctors = doctorService.getAllDoctors();
        model.addAttribute("listDoctors", listDoctors);

        Map<String, String> doctorSpecializations = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            SpecializationInfo specializationInfo = doctorService.getSpecializationCodes().get(doctor.getSpecialist());
            doctorSpecializations.put(doctor.getId(), specializationInfo.getDescription());
        }
        model.addAttribute("doctorSpecializations", doctorSpecializations);

        Map<String, List<Date>> doctorSchedules = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            List<Date> nextFourWeeks = doctorService.getNextFourWeeks(doctor);
            doctorSchedules.put(doctor.getId(), nextFourWeeks); 
        }
        model.addAttribute("doctorSchedules", doctorSchedules); 

        return "form-add-patient-appointment"; 
    }

    
    @PostMapping("/create-with-patient")
    public String createAppointmentWithPatient(@Valid @ModelAttribute("AddPatientAppointmentRequestDTO") AddPatientAppointmentRequestDTO appointmentWithNewPatientRequestDTO, Model model) {
        model.addAttribute("page", "appointments");
                                        
        if (patientService.getPatientByNik(appointmentWithNewPatientRequestDTO.getNik()) == null) {
            Appointment savedAppointment = appointmentService.createAppointmentWithPatient(appointmentWithNewPatientRequestDTO);

            model.addAttribute("responseMessage", String.format("Pasien %s dengan ID %s berhasil ditambahkan. Appointment untuk pasien ini juga berhasil dibuat dengan ID appointment %s.", savedAppointment.getPatient().getName(), savedAppointment.getPatient().getId(), savedAppointment.getId()));
            return "response-appointment";
        } else {
            model.addAttribute("responseMessage", String.format("Pasien dengan NIK %s sudah ada. Appointment gagal dibuat.", appointmentWithNewPatientRequestDTO.getNik()));

            return formCreateAppointmentWithPatient(model);
        }
    }

    @GetMapping("/{id}")
    public String detailAppointment(@PathVariable String id, Model model) {
        model.addAttribute("page", "appointments");
        Appointment appointment = appointmentService.getAppointmentById(id);

        model.addAttribute("appointment", formatAppointment(appointment));
        model.addAttribute("editable", false);

        return "view-appointment"; 
    }

    @GetMapping("/{nik}/create") 
    public String formCreateAppointmentWithExistingPatient(@PathVariable String nik, Model model) {
        model.addAttribute("page", "appointments");
        Patient patient = patientService.getPatientByNik(nik);
        model.addAttribute("patient", patient);

        AddAppointmentRequestDTO appointmentWithExistingPatientRequestDTO = new AddAppointmentRequestDTO();
        appointmentWithExistingPatientRequestDTO.setNik(nik);
        model.addAttribute("AddAppointmentRequestDTO", appointmentWithExistingPatientRequestDTO);

        List<Doctor> listDoctors = doctorService.getAllDoctors();
        model.addAttribute("listDoctors", listDoctors);

        Map<String, String> doctorSpecializations = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            SpecializationInfo specializationInfo = doctorService.getSpecializationCodes().get(doctor.getSpecialist());
            doctorSpecializations.put(doctor.getId(), specializationInfo.getDescription());
        }
        model.addAttribute("doctorSpecializations", doctorSpecializations);

        Map<String, List<Date>> doctorSchedules = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            List<Date> nextFourWeeks = doctorService.getNextFourWeeks(doctor);
            doctorSchedules.put(doctor.getId(), nextFourWeeks); 
        }
        model.addAttribute("doctorSchedules", doctorSchedules);

        return "form-add-appointment";
    }

    @PostMapping("/{nik}/create") 
    public String createAppointmentWithExistingPatient(@PathVariable String nik, @Valid @ModelAttribute("AddAppointmentRequestDTO") AddAppointmentRequestDTO appointmentWithExistingPatientRequestDTO, Model model) {
        model.addAttribute("page", "appointments");
        Appointment appointment = new Appointment();
        appointment.setPatient(patientService.getPatientByNik(appointmentWithExistingPatientRequestDTO.getNik()));
        appointment.setDoctor(doctorService.getDoctorById(appointmentWithExistingPatientRequestDTO.getDoctorId()));
        appointment.setDate(appointmentWithExistingPatientRequestDTO.getAppointmentDate());
        appointment.setStatus(0); // default status created
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
        model.addAttribute("page", "appointments");

        Appointment appointment = appointmentService.getAppointmentById(id);

        if (isAppointmentWithin24Hours(appointment.getDate())) {
            model.addAttribute("responseMessage", 
                String.format("Appointment dengan ID %s tidak dapat diubah karena kurang dari 24 jam dari waktu appointment.", appointment.getId()));
            return "response-appointment";
        }

        var appointmentRequestDTO = new UpdateAppointmentRequestDTO();
        appointmentRequestDTO.setId(appointment.getId());
        appointmentRequestDTO.setDoctorId(appointment.getDoctor().getId());
        appointmentRequestDTO.setAppointmentDate(appointment.getDate());

        model.addAttribute("UpdateAppointmentRequestDTO", appointmentRequestDTO);

        List<Doctor> listDoctors = doctorService.getAllDoctors();
        model.addAttribute("listDoctors", listDoctors);

        Map<String, String> doctorSpecializations = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            SpecializationInfo specializationInfo = doctorService.getSpecializationCodes().get(doctor.getSpecialist());
            doctorSpecializations.put(doctor.getId(), specializationInfo.getDescription());
        }
        model.addAttribute("doctorSpecializations", doctorSpecializations);

        Map<String, List<Date>> doctorSchedules = new HashMap<>();
        for (Doctor doctor : listDoctors) {
            List<Date> nextFourWeeks = doctorService.getNextFourWeeks(doctor);
            doctorSchedules.put(doctor.getId(), nextFourWeeks); 
        }
        model.addAttribute("doctorSchedules", doctorSchedules);

        return "form-update-appointment";
    }

    @PostMapping("/update")
    public String updateAppointment(@Valid @ModelAttribute("UpdateAppointmentRequestDTO") UpdateAppointmentRequestDTO appointmentRequestDTO, Model model) {
        model.addAttribute("page", "appointments");

        Appointment appointmentFromDTO = appointmentService.getAppointmentById(appointmentRequestDTO.getId());
        appointmentFromDTO.setId(appointmentRequestDTO.getId());
        appointmentFromDTO.setDoctor(doctorService.getDoctorById(appointmentRequestDTO.getDoctorId()));
        appointmentFromDTO.setTotalFee(doctorService.getDoctorById(appointmentRequestDTO.getDoctorId()).getFee());
        appointmentFromDTO.setDate(appointmentRequestDTO.getAppointmentDate());

        var appointment = appointmentService.updateAppointment(appointmentFromDTO);

        model.addAttribute("responseMessage", 
            String.format("Appointment dengan ID %s berhasil diupdate.", appointment.getId()));
        return "response-appointment";                     
    }

 
   
    private boolean isAppointmentWithin24Hours(Date appointmentDate) {

        // Konversi Date ke Calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointmentDate);

        // Buat LocalDate dari Calendar
        LocalDate appointmentLocalDate = LocalDate.of(cal.get(Calendar.YEAR), 
                                                        cal.get(Calendar.MONTH) + 1, 
                                                        cal.get(Calendar.DAY_OF_MONTH));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        return appointmentLocalDate.isBefore(tomorrow) || appointmentLocalDate.isEqual(tomorrow);
    }
    
    @GetMapping("/{id}/note")
    public String viewAppointmentNoteForm(@PathVariable String id, Model model) {
        model.addAttribute("page", "appointments");

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
    
        model.addAttribute("page", "appointments");

        if (diagnosisTreatmentDTO.getTreatmentIds() == null) {
            diagnosisTreatmentDTO.setTreatmentIds(new ArrayList<>());
        }
        Appointment appointment = appointmentService.getAppointmentById(diagnosisTreatmentDTO.getId());
        model.addAttribute("appointment", formatAppointment(appointment));
    
        if (addRow != null) {
            diagnosisTreatmentDTO.getTreatmentIds().add(null);
        } else if (deleteRow != null) {
            Long removedTreatmentId = diagnosisTreatmentDTO.getTreatmentIds().remove(deleteRow.intValue());
            if (removedTreatmentId != null) {
                Treatment removedTreatment = treatmentService.getTreatmentById(removedTreatmentId);
                appointment.setTotalFee(appointment.getTotalFee() - removedTreatment.getPrice());
            }
        } else {
            appointment.setDiagnosis(diagnosisTreatmentDTO.getDiagnosis());
            
            List<Treatment> treatments = diagnosisTreatmentDTO.getTreatmentIds().stream()
                .filter(Objects::nonNull)
                .map(treatmentService::getTreatmentById)
                .collect(Collectors.toList());
            appointment.setTreatments(treatments);
    
            long totalTreatmentFee = treatments.stream()
                .mapToLong(Treatment::getPrice)
                .sum();
            appointment.setTotalFee(appointment.getDoctor().getFee() + totalTreatmentFee);
    
            appointmentService.updateDiagnosisTreatment(appointment);
    
            model.addAttribute("responseMessage",
                String.format("Berhasil mencatat diagnosis & treatment untuk appointment ID %s.", appointment.getId()));
            return "response-appointment";
        }
    
        // if adding or deleting a row, re-render the form
        model.addAttribute("UpdateDiagnosisTreatmentRequestDTO", diagnosisTreatmentDTO);
        model.addAttribute("editable", true);
        model.addAttribute("treatments", treatmentService.getAllTreatments());
        return "view-appointment";
    }
    
    @PostMapping("/{id}/done")
    public String updateAppointmentStatusDone(@PathVariable String id, Model model) {
        model.addAttribute("page", "appointments");

        appointmentService.updateStatus(id, 1);        
        model.addAttribute("appointment", formatAppointment(appointmentService.getAppointmentById(id)));
        return "view-appointment";
    }
    
    @PostMapping("/{id}/cancel")
    public String updateAppointmentStatusCancelled(@PathVariable String id, Model model) {
        model.addAttribute("page", "appointments");

        appointmentService.updateStatus(id, 2);    
        model.addAttribute("appointment", formatAppointment(appointmentService.getAppointmentById(id)));    
        return "view-appointment";
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirmationForm(@PathVariable String id, Model model) {
        model.addAttribute("page", "appointments");

         model.addAttribute("appointment", formatAppointment(appointmentService.getAppointmentById(id)));
        
        return "confirmation-delete-appointment :: deleteConfirmContent";
    }
    
    @PostMapping("/delete") 
    public String deleteAppointment(@RequestParam String id, Model model) {
        model.addAttribute("page", "appointments");

        Appointment appointment = appointmentService.getAppointmentById(id);
        appointmentService.deleteAppointment(appointment);
        model.addAttribute("responseMessage", String.format("Appointment dengan ID %s berhasil dihapus.", id));
        return "response-appointment";
    }

    @GetMapping("/stat")
    public String getStatisticAppointment(
            @RequestParam(required = false, defaultValue = "Monthly") String period,
            @RequestParam(required = false, defaultValue = "2024") int year,
            Model model) {
        try {
            model.addAttribute("page", "appointments");

            Map<String, Object> statistics = appointmentService.getAppointmentStatisticsFromRest(period, year);
            model.addAttribute("statistics", statistics);
            model.addAttribute("period", period);
            model.addAttribute("year", year);
            return "statistics-appointment"; 

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "response-error-rest";
        } 
    }
}


