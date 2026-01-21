package com.dresstyle.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer //Le indica a Spring que este microservicio será el cerebro para
                    // la redirección de microservicios

public class DiscoveryserverApplication {

	public static void main(String[] args) {
        SpringApplication.run(DiscoveryserverApplication.class, args);
	}

}
