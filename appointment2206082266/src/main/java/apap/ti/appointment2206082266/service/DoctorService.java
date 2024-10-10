package apap.ti.appointment2206082266.service;

import java.util.List;
import java.util.Map;

import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.SpecializationInfo;

public interface DoctorService {
    Doctor addDoctor(Doctor doctor);
    String generateDoctorId(Integer specialist);
    Map<Integer, SpecializationInfo> getSpecializationCodes();
    List<Doctor> getAllDoctors();

}
