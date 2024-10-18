package apap.ti.appointment2206082266.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti.appointment2206082266.model.Treatment;
import apap.ti.appointment2206082266.repository.TreatmentDb;

@Service
public class TreatmentServiceImpl implements TreatmentService {
    @Autowired
    private TreatmentDb treatmentDb;

    @Override
    public List<Treatment> getAllTreatments() {
        return treatmentDb.findAll();
    }

    @Override
    public List<String> getAllTreatmentNames() { 
        return getAllTreatments().stream()
                .map(Treatment::getName)
                .collect(Collectors.toList());
    }    

    @Override
    public Treatment getTreatmentById(Long id) {
        return treatmentDb.findById(id).get();
    }
}
