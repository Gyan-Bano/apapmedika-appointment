package apap.ti.appointment2206082266.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.SpecializationInfo;
import apap.ti.appointment2206082266.repository.DoctorDb;

@Service
public class DoctorServiceImpl implements DoctorService{
    @Autowired
    DoctorDb doctorDb;

    private static final Map<Integer, SpecializationInfo> SPECIALIZATION_CODES = new HashMap<>();
    static {
        SPECIALIZATION_CODES.put(1, new SpecializationInfo("UMM", "Dokter Umum"));
        SPECIALIZATION_CODES.put(2, new SpecializationInfo("PDL", "Penyakit Dalam"));
        SPECIALIZATION_CODES.put(3, new SpecializationInfo("GGI", "Dokter Gigi"));
        SPECIALIZATION_CODES.put(4, new SpecializationInfo("PRU", "Paru"));
        SPECIALIZATION_CODES.put(5, new SpecializationInfo("ANK", "Spesialis Anak"));
        SPECIALIZATION_CODES.put(6, new SpecializationInfo("THT", "Telinga, Hidung, Tenggorokan, Bedah Kepala Leher"));
        SPECIALIZATION_CODES.put(7, new SpecializationInfo("BDH", "Spesialis Bedah"));
        SPECIALIZATION_CODES.put(8, new SpecializationInfo("KSJ", "Kesehatan Jiwa"));
        SPECIALIZATION_CODES.put(9, new SpecializationInfo("PRE", "Bedah Plastik, Rekonstruksi, dan Estetik"));
        SPECIALIZATION_CODES.put(10, new SpecializationInfo("ANS", "Anestesi"));
        SPECIALIZATION_CODES.put(11, new SpecializationInfo("JPD", "Jantung dan Pembuluh Darah"));
        SPECIALIZATION_CODES.put(12, new SpecializationInfo("NRO", "Neurologi"));
        SPECIALIZATION_CODES.put(13, new SpecializationInfo("KKL", "Kulit dan Kelamin"));
        SPECIALIZATION_CODES.put(14, new SpecializationInfo("URO", "Urologi"));
        SPECIALIZATION_CODES.put(15, new SpecializationInfo("MTA", "Mata"));
        SPECIALIZATION_CODES.put(16, new SpecializationInfo("OBG", "Obstetri dan Ginekologi"));
        SPECIALIZATION_CODES.put(17, new SpecializationInfo("RAD", "Radiologi"));
    }

    private static final Map<Integer, String> DAY_MAP = new HashMap<>();
    static {
        DAY_MAP.put(1, "Monday");
        DAY_MAP.put(2, "Tuesday");
        DAY_MAP.put(3, "Wednesday");
        DAY_MAP.put(4, "Thursday");
        DAY_MAP.put(5, "Friday");
        DAY_MAP.put(6, "Saturday");
        DAY_MAP.put(7, "Sunday");
    }

    
    @Override
    public Map<Integer, SpecializationInfo> getSpecializationCodes() {
        return SPECIALIZATION_CODES;
    }

    @Override
    public String generateDoctorId(Integer specialist) {
        SpecializationInfo specInfo = SPECIALIZATION_CODES.get(specialist);
    
        if (specInfo == null) {
            throw new IllegalArgumentException("Invalid specialist code");
        }
        String specCode = specInfo.getCode(); 

        Doctor lastDoctor = doctorDb.findTopBySpecialistOrderByIdDesc(specialist);

        int sequenceNumber = 1; // Default to 001 if no doctor is found
        if (lastDoctor != null) {
            String lastId = lastDoctor.getId();
            String lastSequence = lastId.substring(3, 6);
            sequenceNumber = Integer.parseInt(lastSequence) + 1;
        }

        String sequence = String.format("%03d", sequenceNumber);
        return specCode + sequence;
    }

    @Override
    public Doctor addDoctor(Doctor doctor) {
        return doctorDb.save(doctor);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorDb.findAll();
    }
}
