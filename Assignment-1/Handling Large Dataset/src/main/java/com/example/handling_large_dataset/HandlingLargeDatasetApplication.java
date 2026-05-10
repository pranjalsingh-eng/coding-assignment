package com.example.handling_large_dataset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HandlingLargeDatasetApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandlingLargeDatasetApplication.class, args);
    }

}
