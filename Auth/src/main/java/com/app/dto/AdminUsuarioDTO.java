package com.app.dto;

import com.app.model.Usuario;

public record AdminUsuarioDTO(
        Long id,
        String username,
        String nombre,
        String apellido,
        String email,
        String rol,
        boolean habilitado
) {
    public static AdminUsuarioDTO from(Usuario usuario) {
        return new AdminUsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().name(),
                !Boolean.FALSE.equals(usuario.getHabilitado())
        );
    }
}
