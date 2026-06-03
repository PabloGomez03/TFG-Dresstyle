package com.dresstyle.subscriptionservice;

import com.dresstyle.subscriptionservice.service.SubscriptionPlanService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableDiscoveryClient
public class SubscriptionserviceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SubscriptionserviceApplication.class, args);
		
		SubscriptionPlanService planService = context.getBean(SubscriptionPlanService.class);
		planService.initializePlans();
	}

}
