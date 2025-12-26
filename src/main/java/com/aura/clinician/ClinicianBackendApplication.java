package com.aura.clinician;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClinicianBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClinicianBackendApplication.class, args);

        System.out.println("---------------Application has been started!---------------");
    }
}
