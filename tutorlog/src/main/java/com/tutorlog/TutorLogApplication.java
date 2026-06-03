package com.tutorlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TutorLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(TutorLogApplication.class, args);
    }
}
