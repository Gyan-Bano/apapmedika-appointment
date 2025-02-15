package apap.ti.appointment2206082266.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import apap.ti.appointment2206082266.model.Appointment;


public interface AppointmentDb extends JpaRepository<Appointment, String>{
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.date >= :startDate AND a.date <= :endDate AND a.deletedAt IS NULL")
    long countActiveAppointmentsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.date >= :startDate AND a.date <= :endDate")
    long countAllAppointmentsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT a FROM Appointment a WHERE a.deletedAt IS NULL AND a.date >= :startDate AND a.date <= :endDate")
    List<Appointment> findActiveAppointmentsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.deletedAt IS NULL")
    List<Appointment> findAllActiveAppointments();
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.deletedAt IS NULL")
    long countAllActiveAppointments();

    @Query("SELECT a FROM Appointment a WHERE a.date >= :startDate AND a.date <= :endDate")
    List<Appointment> findAllAppointmentsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT a FROM Appointment a WHERE a.deletedAt IS NOT NULL AND a.date >= :startDate AND a.date <= :endDate")
    List<Appointment> findDeletedAppointmentsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT a FROM Appointment a WHERE a.status = 0 AND a.deletedAt IS NULL AND a.date >= :startOfDay AND a.date <= :endOfDay")
    List<Appointment> findActiveStatusZeroCreatedAppointmentsForDate(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);
} 

