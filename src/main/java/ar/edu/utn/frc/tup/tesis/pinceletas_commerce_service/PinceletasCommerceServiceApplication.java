package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service",
		"ar.edu.utn.frc.tup.tesis.pinceletas.common.security"
})
public class PinceletasCommerceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PinceletasCommerceServiceApplication.class, args);
	}

}
