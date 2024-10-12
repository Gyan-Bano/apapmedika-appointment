package apap.ti.appointment2206082266.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/patient")
public class PatientController {
    @GetMapping("/search")
    public String searchBarPatient(Model model) {
        return "search-patient";
    }
    
}