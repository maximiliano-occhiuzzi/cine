package com.app.client;

import com.app.config.FeignConfig;
import com.app.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * El evento de venta confirmada que publica Ventas solo trae el username
 * del comprador (no su mail): Gmail resuelve el mail real pegandole a
 * Auth antes de mandar el correo.
 */
@FeignClient(name = "auth", configuration = FeignConfig.class)
public interface AuthClient {

    @GetMapping("/auth/usuarios/{username}")
    UsuarioDTO obtenerUsuario(@PathVariable("username") String username);
}
