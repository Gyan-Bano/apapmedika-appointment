package apap.ti.appointment2206082266.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import apap.ti.appointment2206082266.model.Appointment;


public interface AppointmentDb extends JpaRepository<Appointment, String>{
    List<Appointment> findByDateBetween(Date fromDate, Date toDate);
    long countByDateBetween(Date fromDate, Date toDate);
} 