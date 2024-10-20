package apap.ti.appointment2206082266.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import apap.ti.appointment2206082266.restdto.response.AppointmentResponseDTO;
import apap.ti.appointment2206082266.restdto.response.BaseResponseDTO;
import apap.ti.appointment2206082266.restservice.AppointmentRestService;

import java.util.Date;
import java.util.List;
import java.util.Map; // Fix: Import the correct Map class

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate; // Fix: Import the LocalDate class from java.time package
import java.time.format.TextStyle; // Fix: Import the TextStyle class from java.time.format package
import java.util.Locale; // Fix: Import the Locale class from java.util package
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

            // Set successful response
            statistics.put("data", data);
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("Appointment statistics retrieved successfully");
            baseResponseDTO.setData(statistics);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            // Handle any errors that might occur
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Error retrieving appointment statistics: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
