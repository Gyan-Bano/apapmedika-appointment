package apap.ti.appointment2206082266;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;

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
	
}