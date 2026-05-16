package com.hireconnect.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@org.springframework.cloud.openfeign.EnableFeignClients
@EnableDiscoveryClient
@org.springframework.cache.annotation.EnableCaching
public class JobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }

}
