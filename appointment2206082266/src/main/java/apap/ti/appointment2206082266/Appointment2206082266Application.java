package apap.ti.appointment2206082266;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.service.DoctorService;
import jakarta.transaction.Transactional;
import com.github.javafaker.Faker;


import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SpringBootApplication
public class Appointment2206082266Application {

    public static void main(String[] args) {
        SpringApplication.run(Appointment2206082266Application.class, args);
    }

	@Bean
	public CommandLineRunner initData(JdbcTemplate jdbcTemplate) {
		return args -> {
			Timestamp now = Timestamp.from(Instant.now());
			String sql = """
				INSERT INTO treatment (id, name, price, created_at, updated_at) VALUES
				(1, 'X-ray', 100, ?, ?),
				(2, 'CT Scan', 500, ?, ?),
				(3, 'MRI', 800, ?, ?),
				(4, 'Ultrasound', 150, ?, ?),
				(5, 'Blood Clotting Test', 50, ?, ?),
				(6, 'Blood Glucose Test', 30, ?, ?),
				(7, 'Liver Function Test', 70, ?, ?),
				(8, 'Complete Blood Count', 40, ?, ?),
				(9, 'Urinalysis', 25, ?, ?),
				(10, 'COVID-19 testing', 60, ?, ?),
				(11, 'Cholesterol Test', 45, ?, ?),
				(12, 'Inpatient care', 1000, ?, ?),
				(13, 'Surgery', 5000, ?, ?),
				(14, 'ICU', 2000, ?, ?),
				(15, 'ER', 300, ?, ?),
				(16, 'Flu shot', 20, ?, ?),
				(17, 'Hepatitis vaccine', 75, ?, ?),
				(18, 'COVID-19 Vaccine', 50, ?, ?),
				(19, 'MMR Vaccine', 60, ?, ?),
				(20, 'HPV Vaccine', 80, ?, ?),
				(21, 'Pneumococcal Vaccine', 90, ?, ?),
				(22, 'Herpes Zoster Vaccine', 100, ?, ?),
				(23, 'Physical exam', 100, ?, ?),
				(24, 'Mammogram', 120, ?, ?),
				(25, 'Colonoscopy', 500, ?, ?),
				(26, 'Dental X-ray', 110, ?, ?),
				(27, 'Fillings', 150, ?, ?),
				(28, 'Dental scaling', 200, ?, ?),
				(29, 'Physical therapy', 250, ?, ?),
				(30, 'Occupational therapy', 300, ?, ?),
				(31, 'Speech therapy', 280, ?, ?),
				(32, 'Psychiatric evaluation', 400, ?, ?),
				(33, 'Natural delivery', 3000, ?, ?),
				(34, 'C-section', 7000, ?, ?)
				ON CONFLICT (id) DO NOTHING
			""";
	
			// Create an array to hold all the `now` timestamps for binding
			Object[] params = new Object[34 * 2]; // 34 rows, each with 2 timestamps
			for (int i = 0; i < params.length; i++) {
				params[i] = now; // bind the `now` value to each positional parameter
			}
	
			jdbcTemplate.update(sql, params);
			System.out.println("Treatment data initialized successfully.");
		};
	}

	@Bean
	@Transactional
	public CommandLineRunner initializeDoctors(DoctorService doctorService) {
		return args -> {
			Faker faker = new Faker(new Locale("en-US"));
			Random random = new Random();

			// Specialization codes (you may need to adjust these based on your actual codes)
			Integer[] specializations = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};

			for (int i = 0; i < 20; i++) {
				Doctor doctor = new Doctor();
				doctor.setName(faker.name().fullName());
				doctor.setEmail(faker.internet().emailAddress());
				doctor.setGender(random.nextBoolean());
				doctor.setSpecialist(specializations[random.nextInt(specializations.length)]);
				doctor.setYearsOfExperience(random.nextInt(1, 31)); // 1 to 30 years of experience
				doctor.setFee(Long.valueOf(random.nextInt(100, 1001))); // Fee between 100 and 1000

				// Generate random schedules (1-7 days)
				List<Integer> schedules = new ArrayList<>();
				int numDays = random.nextInt(1, 8); // 1 to 7 days
				for (int j = 0; j < numDays; j++) {
					int day;
					do {
						day = random.nextInt(1, 8); // 1 (Monday) to 7 (Sunday)
					} while (schedules.contains(day));
					schedules.add(day);
				}
				Collections.sort(schedules);
				doctor.setSchedules(schedules);

				// Generate doctor ID (you may need to adjust this based on your actual ID generation logic)
				String doctorId = doctorService.generateDoctorId(doctor.getSpecialist());
				doctor.setId(doctorId);

				doctorService.addDoctor(doctor);
				System.out.println("Added doctor: " + doctor.getName() + " (ID: " + doctor.getId() + ")");
			}

			System.out.println("Doctor data initialized successfully.");
		};
	}
	
}