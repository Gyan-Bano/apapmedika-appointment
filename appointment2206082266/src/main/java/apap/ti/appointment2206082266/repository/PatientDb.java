package apap.ti.appointment2206082266.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import apap.ti.appointment2206082266.model.Patient;


public interface PatientDb extends JpaRepository<Patient, UUID>{
    Patient findByNik(String nik);
    
} 