package com.bakery.bakeryapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BakeryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryApiApplication.class, args);
    }

}


