package apap.ti.appointment2206082266.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDiagnosisTreatmentRequestDTO {
    @NotNull(message = "ID Appointment tidak boleh kosong")
    private String id;

    @NotNull(message = "Diagnosis tidak boleh kosong")
    private String diagnosis;

    private List<Long> treatmentIds; 
}