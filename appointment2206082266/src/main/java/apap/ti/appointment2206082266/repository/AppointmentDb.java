package apap.ti.appointment2206082266.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import apap.ti.appointment2206082266.model.Appointment;


public interface AppointmentDb extends JpaRepository<Appointment, String>{
    

} 