package com.thoughtmechanix.licenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient  
@EnableCircuitBreaker
public class Application {
	
	//Usato da OrganizationRestTemplateClient , e' gia configurato con tutti endpoints scaricati da eureka server.[Ribbon-backed Spring RestTemplate]
	//Vedi licensing-service_chapter4/Leggime.txt
	//ATTENZIONE a @LoadBalanced!!
	//Questa annotazione permette la risoluzione "organizationservice" =>  10.195.171.96:8080/v1/organizations grazie a LoadBalancerInterceptor.    
	//@EnableEurekaClient abilita Ribbon intercepton nelle istanze  RestTemplate managed da Spring!!

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
