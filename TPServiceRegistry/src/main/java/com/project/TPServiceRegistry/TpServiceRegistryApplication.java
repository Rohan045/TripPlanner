package com.project.TPServiceRegistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class TpServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(TpServiceRegistryApplication.class, args);
	}

}
