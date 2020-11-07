package com.uc3m.fs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.uc3m.fs.storage.fs.StorageService;

@SpringBootApplication
public class FS_Application {

	public static void main(String[] args) {
		SpringApplication.run(FS_Application.class, args);
	}

	// Actions before start
	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			//storageService.deleteAll();
			storageService.init();
		};
	}

}