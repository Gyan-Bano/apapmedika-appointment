package apap.ti.appointment2206082266.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.javafaker.App;

import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.BaseResponseDTO;
import apap.ti.appointment2206082266.restservice.AppointmentRestService;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/appointment")
public class AppointmentRestController {
    @Autowired
    AppointmentRestService appointmentRestService;

    @GetMapping("/all")
    public ResponseEntity<?> listAppointment() {
        var baseResponseDTO = new BaseResponseDTO<List<AppointmentResponseDTO>>();
        List<AppointmentResponseDTO> listAppointment = appointmentRestService.getAllAppointment();
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setMessage(String.format("List Appointment berhasil ditemukan"));
        baseResponseDTO.setData(listAppointment);
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }
    
}
