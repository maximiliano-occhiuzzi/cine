package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;

/**
 * Los microservicios rechazan (403) todo lo que no traiga la cabecera
 * X-Gateway-Secret (ver GatewayInterceptor). Gmail le pega a Auth por
 * Feign para resolver el mail del comprador (el evento de Ventas solo
 * trae el username) sin pasar por el Gateway, asi que tiene que mandar
 * esa cabecera el mismo. El secreto es el mismo gateway.internal-secret
 * que viene del Config Server.
 */
public class FeignConfig {

    @Bean
    public RequestInterceptor gatewaySecretInterceptor(@Value("${gateway.internal-secret}") String secret) {
        return template -> template.header("X-Gateway-Secret", secret);
    }
}
