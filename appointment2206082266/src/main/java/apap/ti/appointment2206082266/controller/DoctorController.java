package apap.ti.appointment2206082266.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import apap.ti.appointment2206082266.dto.request.AddDoctorRequestDTO;
import apap.ti.appointment2206082266.dto.request.UpdateDoctorRequestDTO;
import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.SpecializationInfo;
import apap.ti.appointment2206082266.model.Treatment;
import apap.ti.appointment2206082266.service.DoctorService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    private static final Map<Integer, String> STATUS_MAP;

    static {
        STATUS_MAP = new HashMap<>();
        STATUS_MAP.put(0, "Created");
        STATUS_MAP.put(1, "Done");
        STATUS_MAP.put(2, "Cancelled");
    }

    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d MMMM yyyy HH:mm");

    private static final Map<String, Integer> DAY_TO_NUMBER = new LinkedHashMap<>();
    static {
        DAY_TO_NUMBER.put("Monday", 1);
        DAY_TO_NUMBER.put("Tuesday", 2);
        DAY_TO_NUMBER.put("Wednesday", 3);
        DAY_TO_NUMBER.put("Thursday", 4);
        DAY_TO_NUMBER.put("Friday", 5);
        DAY_TO_NUMBER.put("Saturday", 6);
        DAY_TO_NUMBER.put("Sunday", 7);
    }

    private static final Map<Integer, String> NUMBER_TO_DAY = new LinkedHashMap<>();
    static {
        for (Map.Entry<String, Integer> entry : DAY_TO_NUMBER.entrySet()) {
            NUMBER_TO_DAY.put(entry.getValue(), entry.getKey());
        }
    }

    private Map<String, Object> formatAppointmentForDoctorView(Appointment appointment) {
        Map<String, Object> formattedAppointment = new HashMap<>();
        formattedAppointment.put("patientName", appointment.getPatient().getName());
        formattedAppointment.put("diagnosis", appointment.getDiagnosis());
        formattedAppointment.put("totalFee", appointment.getTotalFee());
        formattedAppointment.put("status", STATUS_MAP.get(appointment.getStatus()));
        formattedAppointment.put("appointmentDate", dateFormat.format(appointment.getDate()));
        
        String treatments = Optional.ofNullable(appointment.getTreatments())
            .filter(list -> !list.isEmpty())
            .map(list -> list.stream()
                .map(Treatment::getName)
                .collect(Collectors.joining(", ")))
            .orElse("");
        formattedAppointment.put("treatments", treatments);
        
        return formattedAppointment;
    }

    @GetMapping("/all")
    public String viewAllDoctors(Model model) {
        model.addAttribute("page", "doctors");

        List<Doctor> listDoctors = doctorService.getAllDoctors();
        Map<Integer, SpecializationInfo> specializationCodes = doctorService.getSpecializationCodes();
    
        List<Map<String, Object>> formattedDoctors = listDoctors.stream().map(doctor -> {
            Map<String, Object> formattedDoctor = new HashMap<>();
            formattedDoctor.put("id", doctor.getId());
            formattedDoctor.put("name", doctor.getName());
            formattedDoctor.put("specialization", specializationCodes.get(doctor.getSpecialist()).getDescription());
            formattedDoctor.put("schedules", doctor.getSchedules().stream()
                .map(NUMBER_TO_DAY::get)  // convert schedule from number to day name
                .collect(Collectors.joining(", ")));
            return formattedDoctor;
        }).collect(Collectors.toList());
    
        model.addAttribute("listDoctors", formattedDoctors);
    
        List<String> specializations = specializationCodes.values().stream()
            .map(SpecializationInfo::getDescription)
            .collect(Collectors.toList());
        model.addAttribute("specializations", specializations);
    
        model.addAttribute("daysOfWeek", NUMBER_TO_DAY.values());
    
        return "viewall-doctors";  
    }
    
    @GetMapping("/create")
    public String formCreateDoctor(Model model) {
        model.addAttribute("page", "doctors");

        var doctorRequestDTO = new AddDoctorRequestDTO();
        model.addAttribute("AddDoctorRequestDTO", doctorRequestDTO);
        model.addAttribute("dayToNumber", DAY_TO_NUMBER);
        Map<Integer, SpecializationInfo> specializationCodes = doctorService.getSpecializationCodes();

        model.addAttribute("specializationCodes", specializationCodes);

        return "form-add-doctor";
    }

    @PostMapping("/create")
    public String createDoctor(
        @ModelAttribute("AddDoctorRequestDTO") AddDoctorRequestDTO doctorRequestDTO,
        @RequestParam(value = "addRow", required = false) String addRow,
        @RequestParam(value = "deleteRow", required = false) Integer deleteRow,
        Model model) {
        
        model.addAttribute("page", "doctors");
        if (doctorRequestDTO.getSchedules() == null) {
            doctorRequestDTO.setSchedules(new ArrayList<>());
        }

        if (addRow != null) {
            doctorRequestDTO.getSchedules().add(null);  
        }
        else if (deleteRow != null) {
            doctorRequestDTO.getSchedules().remove(deleteRow.intValue());  
        }
        else {
            Doctor doctor = new Doctor();
            doctor.setName(doctorRequestDTO.getName());
            doctor.setEmail(doctorRequestDTO.getEmail());
            doctor.setGender(doctorRequestDTO.getGender());
            doctor.setSpecialist(doctorRequestDTO.getSpecialist());
            doctor.setYearsOfExperience(doctorRequestDTO.getYearsOfExperience());
            doctor.setFee(doctorRequestDTO.getFee());
            
            String doctorId = doctorService.generateDoctorId(doctor.getSpecialist());
            doctor.setId(doctorId);
            doctor.setSchedules(doctorRequestDTO.getSchedules());
            doctorService.addDoctor(doctor);

            model.addAttribute("responseMessage", 
                String.format("Doctor %s dengan ID %s berhasil ditambahkan.", doctor.getName(), doctor.getId()));
            return "response-doctor";
        }

        // if adding or deleting rows, just re-render the form
        model.addAttribute("AddDoctorRequestDTO", doctorRequestDTO);
        model.addAttribute("dayToNumber", DAY_TO_NUMBER);  
        model.addAttribute("specializationCodes", doctorService.getSpecializationCodes());
        return "form-add-doctor";
    }


    @GetMapping("/{id}")
    public String detailDoctor(@PathVariable String id, Model model) {
        model.addAttribute("page", "doctors");

        Doctor doctor = doctorService.getDoctorById(id);
       
        Map<String, Object> formattedDoctor = new HashMap<>();
        formattedDoctor.put("id", doctor.getId());
        formattedDoctor.put("name", doctor.getName());
        formattedDoctor.put("email", doctor.getEmail());
        formattedDoctor.put("gender", doctor.getGender() ? "Female" : "Male");
        formattedDoctor.put("specialization", doctorService.getSpecializationCodes().get(doctor.getSpecialist()).getDescription());
        formattedDoctor.put("yearsOfExperience", doctor.getYearsOfExperience());
        formattedDoctor.put("fee", doctor.getFee());
        formattedDoctor.put("schedules", doctor.getSchedules().stream()
                .map(NUMBER_TO_DAY::get)
                .collect(Collectors.joining(", ")));
        
        formattedDoctor.put("createdDate", dateTimeFormat.format(doctor.getCreatedAt()));
        formattedDoctor.put("updatedDate", dateTimeFormat.format(doctor.getUpdatedAt()));

        model.addAttribute("doctor", formattedDoctor);
        List<Map<String, Object>> formattedAppointments = doctor.getAppointments().stream()
        .map(this::formatAppointmentForDoctorView)
        .collect(Collectors.toList());

        formattedDoctor.put("appointments", formattedAppointments);
        return "view-doctor";
    }

    @GetMapping("/{id}/update")
    public String updateDoctorForm(@PathVariable String id, Model model) {
        model.addAttribute("page", "doctors");

        Doctor doctor = doctorService.getDoctorById(id);
        var doctorRequestDTO = new UpdateDoctorRequestDTO();
        doctorRequestDTO.setId(doctor.getId());
        doctorRequestDTO.setName(doctor.getName());
        doctorRequestDTO.setSpecialist(doctor.getSpecialist());
        doctorRequestDTO.setEmail(doctor.getEmail());
        doctorRequestDTO.setGender(doctor.getGender());
        doctorRequestDTO.setYearsOfExperience(doctor.getYearsOfExperience());
        doctorRequestDTO.setSchedules(doctor.getSchedules());
        doctorRequestDTO.setFee(doctor.getFee());
        model.addAttribute("dayToNumber", DAY_TO_NUMBER);
        model.addAttribute("UpdateDoctorRequestDTO", doctorRequestDTO);
        Map<Integer, SpecializationInfo> specializationCodes = doctorService.getSpecializationCodes();

        model.addAttribute("specializationCodes", specializationCodes);
        return "form-update-doctor";
    }
    
    @PostMapping("/update")
    public String updateDoctor(
        @ModelAttribute("UpdateDoctorRequestDTO") UpdateDoctorRequestDTO doctorRequestDTO,
        @RequestParam(value = "addRow", required = false) String addRow,
        @RequestParam(value = "deleteRow", required = false) Integer deleteRow,
        Model model) {
    
        model.addAttribute("page", "doctors");

        if (doctorRequestDTO.getSchedules() == null) {
            doctorRequestDTO.setSchedules(new ArrayList<>());
        }
    
        if (addRow != null) {
            doctorRequestDTO.getSchedules().add(null); 
        }
        else if (deleteRow != null) {
            doctorRequestDTO.getSchedules().remove(deleteRow.intValue()); 
        }
        else {
            Doctor doctor = new Doctor();
            doctor.setId(doctorRequestDTO.getId());
            doctor.setName(doctorRequestDTO.getName());
            doctor.setSpecialist(doctorRequestDTO.getSpecialist());
            doctor.setEmail(doctorRequestDTO.getEmail());
            doctor.setGender(doctorRequestDTO.getGender());
            doctor.setYearsOfExperience(doctorRequestDTO.getYearsOfExperience());
            doctor.setFee(doctorRequestDTO.getFee());
            
            doctor.setSchedules(doctorRequestDTO.getSchedules());
    
            doctorService.updateDoctor(doctor);
    
            model.addAttribute("responseMessage", String.format("Doctor %s dengan ID %s berhasil diupdate.", doctor.getName(), doctor.getId()));
            return "response-doctor";
        }
    
        // if adding or deleting rows, re-render the form
        model.addAttribute("UpdateDoctorRequestDTO", doctorRequestDTO);
        model.addAttribute("dayToNumber", DAY_TO_NUMBER);
        model.addAttribute("specializationCodes", doctorService.getSpecializationCodes());
    
        return "form-update-doctor";
    }
    

    @GetMapping("/{id}/delete")
    public String deleteConfirmationForm(@PathVariable String id, Model model) {
        model.addAttribute("page", "doctors");

        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        // load deleteconfirmcontent sebagai model jadi hanya sebagian gitu
        return "confirmation-delete-doctor :: deleteConfirmContent";
    }


    @PostMapping("/delete")
    public String deleteDoctor(@RequestParam String id, Model model) {
        model.addAttribute("page", "doctors");

        Doctor doctor = doctorService.getDoctorById(id);
        doctorService.deleteDoctor(doctor);
        model.addAttribute("responseMessage", 
        String.format("Doctor %s dengan ID %s berhasil dihapus.", doctor.getName(), doctor.getId()));
        
        return "response-doctor";
    }
}