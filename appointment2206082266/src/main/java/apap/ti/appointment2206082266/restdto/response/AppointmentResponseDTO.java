package apap.ti.appointment2206082266.restdto.response;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppointmentResponseDTO {
    private String id;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DoctorResponseDTO doctor;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PatientResponseDTO patient;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Jakarta")
    private Date date;
    
    private String diagnosis;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<TreatmentResponseDTO> treatments;
    
    private Long totalFee;
    
    private Integer status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Jakarta")
    private Date createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Jakarta")
    private Date updatedAt;
}