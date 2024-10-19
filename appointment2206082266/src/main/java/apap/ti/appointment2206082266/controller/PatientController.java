package apap.ti.appointment2206082266.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import apap.ti.appointment2206082266.model.Patient;
import apap.ti.appointment2206082266.service.PatientService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.text.SimpleDateFormat;



@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    @GetMapping("/search")
    public String searchBarPatient(Model model) {

        return "search-patient";
    }
    
    @PostMapping("/search")
    public String searchPatient(@RequestParam String nik, Model model) {
        Patient patient = patientService.getPatientByNik(nik);
        
        if (patient != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
            model.addAttribute("patient", patient);
            model.addAttribute("gender", patient.getGender() == true ? "Female" : "Male");
            model.addAttribute("formattedBirthDate", dateFormat.format(patient.getBirthDate()));
            return "found-patient";
        } else {
            return "found-no-patient";
        }
    }  
}