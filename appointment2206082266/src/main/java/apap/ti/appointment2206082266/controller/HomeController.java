package apap.ti.appointment2206082266.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import apap.ti.appointment2206082266.service.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;



@Controller
public class HomeController {
    @Autowired AppointmentService appointmentService;

    @GetMapping("/")
    private String home(Model model) {
        model.addAttribute("page", "home");
        model.addAttribute("onGoingAppointmentToday", appointmentService.getTodayActiveStatusZeroCreatedAppointments().size());
        return "home";
    }
}
