package apap.ti.appointment2206082266.repository;

import apap.ti.appointment2206082266.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentDb extends JpaRepository<Treatment, Long> {
}