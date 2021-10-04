package com.dxc.application.databasestructure;

import com.dxc.application.databasestructure.service.DatabaseStructureService;
import com.dxc.application.databasestructure.service.MySqlDatabaseStructureService;
import com.dxc.application.databasestructure.service.OracleDatabaseStructureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class Application {
    @Value("${db.brand}")
    private String dbBrand;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            DatabaseStructureService excelService = null;
            if(StringUtils.equalsIgnoreCase(dbBrand,"MYSQL")){
                excelService = ctx.getBean(MySqlDatabaseStructureService.class);
            }else {
                excelService = ctx.getBean(OracleDatabaseStructureService.class);
            }

            try {
                excelService.createExcel();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
    }
}
