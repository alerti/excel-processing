package com.example.excelprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ExcelProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelProcessingApplication.class, args);
    }

}
