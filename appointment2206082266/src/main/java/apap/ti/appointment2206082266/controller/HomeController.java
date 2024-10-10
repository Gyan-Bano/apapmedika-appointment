package apap.ti.appointment2206082266.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;



@Controller
public class HomeController {
    @GetMapping("/")
    private String home(Model model) {
        return "home";
    }
}
