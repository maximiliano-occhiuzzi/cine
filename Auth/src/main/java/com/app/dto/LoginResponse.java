package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private String refreshToken;
    private String username;
    private String nombre;
    private String apellido;
    private String rol;
    private long expiresInSeconds;
}
