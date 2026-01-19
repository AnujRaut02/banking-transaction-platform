package com.banking.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BankingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingPlatformApplication.class, args);
	}

}
