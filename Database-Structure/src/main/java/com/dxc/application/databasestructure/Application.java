package com.dxc.application.databasestructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            DatabaseStructureService excelService = ctx.getBean(DatabaseStructureService.class);
            try {
                excelService.createExcel();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
    }
}
