package com.gs.ayanami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.gs.ayanami.repository")
@EntityScan(basePackages = "com.gs.ayanami.model")
public class AyanamiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AyanamiApplication.class, args);
    }
}
