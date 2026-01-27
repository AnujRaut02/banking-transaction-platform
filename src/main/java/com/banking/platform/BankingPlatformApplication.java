package com.banking.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = "com.banking.platform")
@EnableCaching
@EnableKafka
public class BankingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingPlatformApplication.class, args);
	}

}
