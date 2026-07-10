package com.geosecure.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Phase 3 addition: @EnableScheduling turns on the fixed-rate sweep job in
 * AttendanceSessionScheduler (automatic QR/session expiry closing). No other
 * change to this class.
 */
@SpringBootApplication
@EnableScheduling
public class AttendanceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }
}
