package apap.ti.appointment2206082266.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAppointmentRequestDTO extends AddAppointmentRequestDTO {
     @NotNull(message = "ID Appointment tidak boleh kosong")
    private String id;
    
}
