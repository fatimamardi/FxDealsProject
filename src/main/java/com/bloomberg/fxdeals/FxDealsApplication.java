package com.bloomberg.fxdeals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Bloomberg FX Deals Data Warehouse
 * 
 * This application provides a REST API for importing and persisting FX deal data
 * with validation, duplicate detection, and no-rollback transaction handling.
 */
@SpringBootApplication(scanBasePackages = "com.bloomberg.fxdeals")
@EnableTransactionManagement
public class FxDealsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FxDealsApplication.class, args);
    }
}

