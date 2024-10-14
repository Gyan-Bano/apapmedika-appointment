package apap.ti.appointment2206082266.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti.appointment2206082266.repository.PatientDb;
import apap.ti.appointment2206082266.model.Patient;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientDb patientDb;

    @Override
    public Patient getPatientByNik(String nik) {
        return patientDb.findByNik(nik);
    }

    @Override
    public Patient addPatient(Patient patient) {
        return patientDb.save(patient);
    }
}
