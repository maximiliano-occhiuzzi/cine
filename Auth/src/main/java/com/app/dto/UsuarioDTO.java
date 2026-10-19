package com.app.dto;

import com.app.model.Usuario;

public record UsuarioDTO(String username, String nombre, String apellido, String email) {
    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(usuario.getUsername(), usuario.getNombre(), usuario.getApellido(), usuario.getEmail());
    }
}
