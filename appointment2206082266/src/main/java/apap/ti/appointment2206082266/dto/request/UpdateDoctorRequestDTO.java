package apap.ti.appointment2206082266.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDoctorRequestDTO extends AddDoctorRequestDTO {

    @NotNull(message = "ID Dokter tidak boleh kosong")
    private String id;
}
