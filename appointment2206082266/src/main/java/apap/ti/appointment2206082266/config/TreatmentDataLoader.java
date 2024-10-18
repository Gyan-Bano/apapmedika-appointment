package apap.ti.appointment2206082266.config;

import apap.ti.appointment2206082266.model.Treatment;
import apap.ti.appointment2206082266.repository.TreatmentDb;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class TreatmentDataLoader {

    @Bean
    CommandLineRunner initDatabase(TreatmentDb treatmentDb) {
        return args -> {
            List<Treatment> treatments = Arrays.asList(
                new Treatment(1L, "X-ray", 100L),
                new Treatment(2L, "CT Scan", 500L),
                new Treatment(3L, "MRI", 800L),
                new Treatment(4L, "Ultrasound", 150L),
                new Treatment(5L, "Blood Clotting Test", 50L),
                new Treatment(6L, "Blood Glucose Test", 30L),
                new Treatment(7L, "Liver Function Test", 70L),
                new Treatment(8L, "Complete Blood Count", 40L),
                new Treatment(9L, "Urinalysis", 25L),
                new Treatment(10L, "COVID-19 testing", 60L),
                new Treatment(11L, "Cholesterol Test", 45L),
                new Treatment(12L, "Inpatient care", 1000L),
                new Treatment(13L, "Surgery", 5000L),
                new Treatment(14L, "ICU", 2000L),
                new Treatment(15L, "ER", 300L),
                new Treatment(16L, "Flu shot", 20L),
                new Treatment(17L, "Hepatitis vaccine", 75L),
                new Treatment(18L, "COVID-19 Vaccine", 50L),
                new Treatment(19L, "MMR Vaccine", 60L),
                new Treatment(20L, "HPV Vaccine", 80L),
                new Treatment(21L, "Pneumococcal Vaccine", 90L),
                new Treatment(22L, "Herpes Zoster Vaccine", 100L),
                new Treatment(23L, "Physical exam", 100L),
                new Treatment(24L, "Mammogram", 120L),
                new Treatment(25L, "Colonoscopy", 500L),
                new Treatment(26L, "Dental X-ray", 110L),
                new Treatment(27L, "Fillings", 150L),
                new Treatment(28L, "Dental scaling", 200L),
                new Treatment(29L, "Physical therapy", 250L),
                new Treatment(30L, "Occupational therapy", 300L),
                new Treatment(31L, "Speech therapy", 280L),
                new Treatment(32L, "Psychiatric evaluation", 400L),
                new Treatment(33L, "Natural delivery", 3000L),
                new Treatment(34L, "C-section", 7000L)
            );

            for (Treatment treatment : treatments) {
                if (treatmentDb.findById(treatment.getId()).isEmpty()) {
                    treatmentDb.save(treatment);
                }
            }
        };
    }
}