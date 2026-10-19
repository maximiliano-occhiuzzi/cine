package com.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


@Component
public class GatewayInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Gateway-Secret";

    @Value("${gateway.internal-secret}")
    private String secretRequired;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String incomingSecret = request.getHeader(HEADER);

        if (incomingSecret == null || !secretsMatch(incomingSecret, secretRequired)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Acceso denegado: las peticiones deben pasar por el Gateway");
            return false;
        }
        return true;
    }

    private boolean secretsMatch(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
