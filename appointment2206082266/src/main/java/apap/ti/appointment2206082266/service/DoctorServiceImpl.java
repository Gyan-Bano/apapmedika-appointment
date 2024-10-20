package apap.ti.appointment2206082266.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti.appointment2206082266.model.Appointment;
import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.model.SpecializationInfo;
import apap.ti.appointment2206082266.repository.DoctorDb;
import apap.ti.appointment2206082266.repository.AppointmentDb;


@Service
public class DoctorServiceImpl implements DoctorService{
    @Autowired
    DoctorDb doctorDb;

    @Autowired
    AppointmentDb appointmentDb;

    private static int globalSequenceNumber = 0;


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

    @Override
    public Map<Integer, SpecializationInfo> getSpecializationCodes() {
        return SPECIALIZATION_CODES;
    }

    @Override
    public synchronized String generateDoctorId(Integer specialist) {
        SpecializationInfo specInfo = SPECIALIZATION_CODES.get(specialist);

        if (specInfo == null) {
            throw new IllegalArgumentException("Invalid specialist code");
        }
        String specCode = specInfo.getCode(); 

        globalSequenceNumber++;

        String sequence = String.format("%03d", globalSequenceNumber % 1000);

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

    @Override
    public Doctor getDoctorById(String id) {
        return doctorDb.findById(id).orElse(null);
    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        Doctor getDoctor = getDoctorById(doctor.getId());
        if (getDoctor != null) {
            getDoctor.setName(doctor.getName());
            getDoctor.setSpecialist(doctor.getSpecialist());
            getDoctor.setEmail(doctor.getEmail());
            getDoctor.setGender(doctor.getGender());
            getDoctor.setYearsOfExperience(doctor.getYearsOfExperience());
            getDoctor.setSchedules(doctor.getSchedules());
            getDoctor.setFee(doctor.getFee()); 
            getDoctor.setUpdatedAt(new Date());
            doctorDb.save(getDoctor);

            return getDoctor;
        }
        return null;
    }

    @Override
    public void deleteDoctor(Doctor doctor) {
        Date now = new Date();

        doctor.setDeletedAt(now);

        // soft delete all related appointments by setting their deletedAt timestamp
        doctor.getAppointments().forEach(appointment -> {
            appointment.setDeletedAt(now);
            appointmentDb.save(appointment);  
        });

        doctorDb.save(doctor);
    }

   @Override
    public List<Date> getNextFourWeeks(Doctor doctor) {
        List<Integer> practiceDays = doctor.getSchedules();
        List<Date> nextFourWeeks = new ArrayList<>();
        List<Date> bookedDates = new ArrayList<>();

        for (Appointment appointment : doctor.getAppointments()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(appointment.getDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            bookedDates.add(cal.getTime());
        }

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 28; i++) { // 4 weeks
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int convertedDayOfWeek = dayOfWeek == 1 ? 7 : dayOfWeek - 1;
            
            if (practiceDays.contains(convertedDayOfWeek)) {
                Calendar currentDate = (Calendar) calendar.clone();
                currentDate.set(Calendar.HOUR_OF_DAY, 0);
                currentDate.set(Calendar.MINUTE, 0);
                currentDate.set(Calendar.SECOND, 0);
                currentDate.set(Calendar.MILLISECOND, 0);
                
                if (!bookedDates.contains(currentDate.getTime())) {
                    nextFourWeeks.add(calendar.getTime());
                }
            }
        }
        return nextFourWeeks;
    }

    @Override
    public void addAppointmentToDoctor(String doctorId, Appointment appointment) {
        Doctor doctor = getDoctorById(doctorId);
        doctor.getAppointments().add(appointment);
        doctorDb.save(doctor);
    }

    @Override
    public void updateAppointmentDoctor(String oldDoctorId, String newDoctorId, Appointment appointment) {
        Doctor oldDoctor = getDoctorById(oldDoctorId);
        oldDoctor.getAppointments().remove(appointment);
        doctorDb.save(oldDoctor);

        Doctor newDoctor = getDoctorById(newDoctorId);
        newDoctor.getAppointments().add(appointment);
        doctorDb.save(newDoctor);
    }
}
