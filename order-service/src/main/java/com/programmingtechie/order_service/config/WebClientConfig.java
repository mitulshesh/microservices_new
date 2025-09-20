package com.programmingtechie.order_service.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

        @Bean
        @LoadBalanced
        public WebClient.Builder webClient(){
            return WebClient.builder();
        }


}
