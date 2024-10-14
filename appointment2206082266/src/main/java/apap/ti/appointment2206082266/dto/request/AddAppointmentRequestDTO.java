package apap.ti.appointment2206082266.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class AddAppointmentRequestDTO {
    @NotNull(message = "NIK tidak boleh kosong")
    private String nik;
    
    @NotNull(message = "ID Dokter tidak boleh kosong")
    private String doctorId;

    @NotNull(message = "Tanggal appointment tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")     
    private Date appointmentDate;

    private String diagnosis;

    private Integer status;
}