package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;


public class MercadoPagoFeignConfig {

    @Bean
    public RequestInterceptor mercadoPagoAuthInterceptor(@Value("${mercadopago.access-token:}") String accessToken) {
        return template -> {
            if (accessToken != null && !accessToken.isBlank()) {
                template.header("Authorization", "Bearer " + accessToken);
            }
        };
    }
}
