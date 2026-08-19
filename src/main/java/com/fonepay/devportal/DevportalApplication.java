package com.fonepay.devportal;

import com.fonepay.devportal.common.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevportalApplication {

	public static void main(String[] args) {
		EnvLoader.load();
		SpringApplication.run(DevportalApplication.class, args);
	}

}
