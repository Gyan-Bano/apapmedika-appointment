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
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.SpecializationInfo;
import apap.ti.appointment2206082266.service.DoctorService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;



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

    @GetMapping("/all")
      public String viewAllDoctors(Model model) {
        List<Doctor> listDoctors = doctorService.getAllDoctors();
        Map<Integer, SpecializationInfo> specializationCodes = doctorService.getSpecializationCodes();

        List<Map<String, Object>> formattedDoctors = listDoctors.stream().map(doctor -> {
            Map<String, Object> formattedDoctor = new HashMap<>();
            formattedDoctor.put("id", doctor.getId());
            formattedDoctor.put("name", doctor.getName());
            formattedDoctor.put("specialization", specializationCodes.get(doctor.getSpecialist()).getDescription());
            formattedDoctor.put("schedules", doctor.getSchedules().stream()
                    .map(NUMBER_TO_DAY::get)
                    .collect(Collectors.joining(", ")));
            return formattedDoctor;
        }).collect(Collectors.toList());

        model.addAttribute("listDoctors", formattedDoctors);
        return "viewall-doctors";
    }
    @GetMapping("/create")
    public String formCreateDoctor(Model model) {
        var doctorRequestDTO = new AddDoctorRequestDTO();
        model.addAttribute("AddDoctorRequestDTO", doctorRequestDTO);
        model.addAttribute("dayToNumber", DAY_TO_NUMBER);
        Map<Integer, SpecializationInfo> specializationCodes = doctorService.getSpecializationCodes();

        model.addAttribute("specializationCodes", specializationCodes);

        return "form-add-doctor";
    }

    @PostMapping("/create")
    public String createDoctor(@ModelAttribute("AddDoctorRequestDTO") AddDoctorRequestDTO doctorRequestDTO, Model model) {
        var doctor = new Doctor();
        doctor.setName(doctorRequestDTO.getName());
        doctor.setSpecialist(doctorRequestDTO.getSpecialist());
        doctor.setEmail(doctorRequestDTO.getEmail());
        doctor.setGender(doctorRequestDTO.getGender());
        doctor.setYearsOfExperience(doctorRequestDTO.getYearsOfExperience());
        doctor.setSchedules(doctorRequestDTO.getSchedules());
        doctor.setFee(doctorRequestDTO.getFee());

        String doctorId = doctorService.generateDoctorId(doctor.getSpecialist());
        doctor.setId(doctorId);
        doctorService.addDoctor(doctor);

        model.addAttribute("responseMessage",
        String.format("Doctor %s dengan ID %s berhasil ditambahkan.", doctor.getName(), doctor.getId()));
        return "response-doctor";
    }

    @GetMapping("/{id}")
    public String detailDoctor(@PathVariable String id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        // if (doctor == null) {
        //     // Handle case where doctor is not found
        //     return "error-page"; // Create this page to show a "Doctor not found" message
        // }

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
        
        // Add appointments if available
        formattedDoctor.put("appointments", doctor.getAppointments());

        // Add metadata
        formattedDoctor.put("createdDate", doctor.getCreatedAt());
        formattedDoctor.put("updatedDate", doctor.getUpdatedAt());

        model.addAttribute("doctor", formattedDoctor);
        return "view-doctor";
    }

    @GetMapping("/{id}/update")
    public String updateDoctorForm(@PathVariable String id, Model model) {
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
    public String updateDoctor(@ModelAttribute UpdateDoctorRequestDTO doctorRequestDTO, Model model) {
        var doctorFromDTO = new Doctor();
        doctorFromDTO.setId(doctorRequestDTO.getId());
        doctorFromDTO.setName(doctorRequestDTO.getName());
        doctorFromDTO.setSpecialist(doctorRequestDTO.getSpecialist());
        doctorFromDTO.setEmail(doctorRequestDTO.getEmail());
        doctorFromDTO.setGender(doctorRequestDTO.getGender());
        doctorFromDTO.setYearsOfExperience(doctorRequestDTO.getYearsOfExperience());
        doctorFromDTO.setSchedules(doctorRequestDTO.getSchedules());
        doctorFromDTO.setFee(doctorRequestDTO.getFee()); 
        
        var doctor = doctorService.updateDoctor(doctorFromDTO);

        model.addAttribute("responseMessage", 
            String.format("Doctor %s dengan ID %s berhasil diupdate.", doctor.getName(), doctor.getId()));
        return "response-doctor";
    }
    
    @GetMapping("/{id}/delete")
    public String deleteConfirmationForm(@PathVariable String id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        // load deleteconfirmcontent sebagai model jadi hanya sebagian gitu
        return "delete-doctor-confirmation :: deleteConfirmContent";
    }


     @PostMapping("/delete")
    public String deleteDoctor(@RequestParam String id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        doctorService.deleteDoctor(doctor);
        model.addAttribute("responseMessage", 
        String.format("Doctor %s dengan ID %s berhasil dihapus.", doctor.getName(), doctor.getId()));
        
        return "response-doctor";
    }
}