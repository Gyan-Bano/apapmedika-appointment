package apap.ti.appointment2206082266;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

import apap.ti.appointment2206082266.model.Doctor;
import apap.ti.appointment2206082266.service.DoctorService;
import org.springframework.transaction.annotation.Transactional;
import com.github.javafaker.Faker;


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