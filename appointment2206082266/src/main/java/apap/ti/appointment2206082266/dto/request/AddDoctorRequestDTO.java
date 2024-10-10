package apap.ti.appointment2206082266.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddDoctorRequestDTO {

    @NotNull(message = "Nama tidak boleh kosong")
    private String name;

    @NotNull(message = "Spesialis tidak boleh kosong")
    private Integer specialist;

    @Email(message = "Format email tidak valid")
    private String email;

    @NotNull(message = "Jenis kelamin tidak boleh kosong")
    private Boolean gender;

    @Min(value = 0, message = "Pengalaman kerja harus minimal 0 tahun")
    @NotNull(message = "Pengalaman kerja tidak boleh kosong")
    private Integer yearsOfExperience;

    @NotNull(message = "Jadwal tidak boleh kosong")
    private List<Integer> schedules;

    @NotNull(message = "Biaya tidak boleh kosong")
    @Min(value = 0, message = "Biaya harus minimal 0")
    private Long fee;
}
