package apap.ti.appointment2206082266.restdto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddAppointmentRequestRestDTO {
    @NotNull(message = "ID Dokter tidak boleh kosong")
    private String doctorId;

    @NotNull(message = "Tanggal appointment tidak boleh kosong")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Jakarta")
    private Date appointmentDate;
}