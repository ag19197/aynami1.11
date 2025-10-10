package com.gs.ayanami;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableScheduling//开启缓存注解功能
@SpringBootApplication
public class AyanamiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AyanamiApplication.class, args);
    }

}
