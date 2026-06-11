package com.fernirx.sneakerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SneakerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SneakerApiApplication.class, args);
    }

}
