package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
		"ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service",
		"ar.edu.utn.frc.tup.tesis.pinceletas.common.security"
})
public class PinceletasCommerceServiceApplication {

	public static void main(String[] args) {
		String accessToken = System.getenv("MERCADOPAGO_ACCESS_TOKEN");
		System.out.println("🟢 Access Token detectado: " + accessToken);

		SpringApplication.run(PinceletasCommerceServiceApplication.class, args);
	}

}
