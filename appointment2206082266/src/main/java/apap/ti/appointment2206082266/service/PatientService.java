package apap.ti.appointment2206082266.service;

import apap.ti.appointment2206082266.model.Patient;

public interface PatientService {
    Patient getPatientByNik(String nik);
    Patient addPatient(Patient patient);
} 