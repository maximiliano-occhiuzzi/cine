package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;


public class FeignConfig {

    @Bean
    public RequestInterceptor gatewaySecretInterceptor(@Value("${gateway.internal-secret}") String secret) {
        return template -> template.header("X-Gateway-Secret", secret);
    }
}
