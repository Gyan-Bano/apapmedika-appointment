package apap.ti.appointment2206082266.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import apap.ti.appointment2206082266.restdto.request.AddAppointmentRequestRestDTO;
import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.BaseResponseDTO;
import apap.ti.appointment2206082266.restservice.AppointmentRestService;
import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;
import java.util.Map; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate; 
import java.time.format.TextStyle; 
import java.util.Locale; 
import java.time.ZoneId;


@RestController
@RequestMapping("/api/appointment")
public class AppointmentRestController {
    @Autowired
    AppointmentRestService appointmentRestService;

    @GetMapping("/all")
    public ResponseEntity<BaseResponseDTO<List<AppointmentResponseDTO>>> getAllAppointments(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {
        try {
            List<AppointmentResponseDTO> appointments = appointmentRestService.getAllAppointments(fromDate, toDate);
            var baseResponseDTO = new BaseResponseDTO<List<AppointmentResponseDTO>>();
            baseResponseDTO.setStatus(200);
            baseResponseDTO.setMessage("Appointments retrieved successfully");
            baseResponseDTO.setData(appointments);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        } catch (Exception e) {
            var baseResponseDTO = new BaseResponseDTO<List<AppointmentResponseDTO>>();
            baseResponseDTO.setStatus(500);
            baseResponseDTO.setMessage("Error retrieving appointments: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/stat")
    public ResponseEntity<?> getAppointmentStatistics(
            @RequestParam String period,
            @RequestParam int year) {
        var baseResponseDTO = new BaseResponseDTO<Map<String, Object>>();
        Map<String, Object> statistics = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();

        try {
            if ("Monthly".equalsIgnoreCase(period)) {
                for (int month = 1; month <= 12; month++) {
                    LocalDate startDate = LocalDate.of(year, month, 1);
                    LocalDate endDate = startDate.plusMonths(1).minusDays(1);
                    long count = appointmentRestService.countAppointmentsByDateRange(
                        Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())
                    );
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("period", startDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                    monthData.put("count", count);
                    data.add(monthData);
                }
            } else if ("Quarterly".equalsIgnoreCase(period)) {
                for (int quarter = 1; quarter <= 4; quarter++) {
                    LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
                    LocalDate endDate = startDate.plusMonths(3).minusDays(1);
                    long count = appointmentRestService.countAppointmentsByDateRange(
                        Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())
                    );
                    Map<String, Object> quarterData = new HashMap<>();
                    quarterData.put("period", "Q" + quarter);
                    quarterData.put("count", count);
                    data.add(quarterData);
                }
            }

            statistics.put("data", data);
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("Appointment statistics retrieved successfully");
            baseResponseDTO.setData(statistics);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Error retrieving appointment statistics: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/detail")
    public ResponseEntity<BaseResponseDTO<AppointmentResponseDTO>> getAppointmentById(@RequestParam String id) {
        try {
            AppointmentResponseDTO appointment = appointmentRestService.getAppointmentById(id);
            if (appointment == null) {
                var baseResponseDTO = new BaseResponseDTO<AppointmentResponseDTO>();
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Appointment not found");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            var baseResponseDTO = new BaseResponseDTO<AppointmentResponseDTO>();
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("Appointment retrieved successfully");
            baseResponseDTO.setData(appointment);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        } catch (Exception e) {
            var baseResponseDTO = new BaseResponseDTO<AppointmentResponseDTO>();
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Error retrieving appointment: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponseDTO<AppointmentResponseDTO>> createAppointment(
            @RequestParam String nik,
            @Valid @RequestBody AddAppointmentRequestRestDTO requestDTO) {
        try {
            AppointmentResponseDTO createdAppointment = appointmentRestService.addAppointment(nik, requestDTO);
            var baseResponseDTO = new BaseResponseDTO<AppointmentResponseDTO>();
            baseResponseDTO.setStatus(201);
            baseResponseDTO.setMessage("Appointment created successfully");
            baseResponseDTO.setData(createdAppointment);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            var baseResponseDTO = new BaseResponseDTO<AppointmentResponseDTO>();
            baseResponseDTO.setStatus(500);
            baseResponseDTO.setMessage("Error creating appointment: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
