package com.app.security;

import com.app.model.RefreshToken;
import com.app.model.Usuario;
import com.app.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken crear(Usuario usuario) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiraEn(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setRevocado(false);
        return refreshTokenRepository.save(refreshToken);
    }

    
    public Optional<Usuario> validarYObtenerUsuario(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevocado())
                .filter(rt -> rt.getExpiraEn().isAfter(Instant.now()))
                .map(RefreshToken::getUsuario);
    }

    
    public void revocar(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(rt -> {
                    rt.setRevocado(true);
                    refreshTokenRepository.save(rt);
                });
    }

    
    public void revocarTodosDe(Usuario usuario) {
        var activos = refreshTokenRepository.findByUsuarioAndRevocadoFalse(usuario);
        activos.forEach(rt -> rt.setRevocado(true));
        refreshTokenRepository.saveAll(activos);
    }
}
