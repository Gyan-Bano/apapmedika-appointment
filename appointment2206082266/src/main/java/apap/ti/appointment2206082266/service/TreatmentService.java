package apap.ti.appointment2206082266.service;

import java.util.List;

import apap.ti.appointment2206082266.model.Treatment;

public interface TreatmentService {
    List<Treatment> getAllTreatments();
    List<String> getAllTreatmentNames();
    Treatment getTreatmentById(Long id); // Added return type
} 