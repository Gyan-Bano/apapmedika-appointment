package apap.ti.appointment2206082266.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import apap.ti.appointment2206082266.model.Doctor;

@Repository
public interface DoctorDb extends JpaRepository<Doctor, String>{
}
