package apap.ti.appointment2206082266.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class AddPatientAppointmentRequestDTO {
    @NotNull(message = "NIK tidak boleh kosong")
    private String nik;

    @NotNull(message = "Nama tidak boleh kosong")
    private String name;

    @NotNull(message = "Jenis kelamin tidak boleh kosong")
    private Boolean gender;

    @Email(message = "Format email tidak valid")
    private String email;

    @NotNull(message = "Tanggal lahir tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd") 
    private Date birthDate;

    @NotNull(message = "Tempat lahir tidak boleh kosong")
    private String birthPlace;

    @NotNull(message = "Tanggal appointment tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd") 
    private Date appointmentDate;

    // Field to bind selected doctor's ID
    @NotNull(message = "ID Dokter tidak boleh kosong")
    private String doctorId;

}
