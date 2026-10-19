package com.app.client;

import com.app.config.FeignConfig;
import com.app.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "auth", configuration = FeignConfig.class)
public interface AuthClient {

    @GetMapping("/auth/usuarios/{username}")
    UsuarioDTO obtenerUsuario(@PathVariable("username") String username);
}
