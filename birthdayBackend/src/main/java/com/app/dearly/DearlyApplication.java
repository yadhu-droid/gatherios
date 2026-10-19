package com.app.dearly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DearlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(DearlyApplication.class, args);
	}

}
