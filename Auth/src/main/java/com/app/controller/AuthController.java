package com.app.controller;

import com.app.dto.ActualizarEstadoUsuarioRequest;
import com.app.dto.ActualizarUsuarioRequest;
import com.app.dto.AdminUsuarioDTO;
import com.app.dto.LoginRequest;
import com.app.dto.LoginResponse;
import com.app.dto.RefreshTokenRequest;
import com.app.dto.RegisterRequest;
import com.app.dto.UsuarioDTO;
import com.app.exception.ApiError;
import com.app.model.RefreshToken;
import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.repository.UsuarioRepository;
import com.app.security.JwtService;
import com.app.security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req);
        }

        if (!estaHabilitado(usuario)) {
            return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req);
        }

        return ResponseEntity.ok(armarRespuesta(usuario));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest req) {
        Usuario usuario = refreshTokenService.validarYObtenerUsuario(request.getRefreshToken())
                .orElse(null);

        if (usuario == null) {
            return error(HttpStatus.UNAUTHORIZED,
                    "Refresh token inválido, vencido o revocado. Iniciá sesión de nuevo.", req);
        }

        if (!estaHabilitado(usuario)) {
            refreshTokenService.revocar(request.getRefreshToken());
            return error(HttpStatus.UNAUTHORIZED,
                    "Usuario deshabilitado. Iniciá sesión de nuevo o contactá a un administrador.", req);
        }

        refreshTokenService.revocar(request.getRefreshToken());

        return ResponseEntity.ok(armarRespuesta(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revocar(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest req) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return error(HttpStatus.CONFLICT, "El usuario ya existe", req);
        }

        Rol rol;
        try {
            rol = request.getRol() != null ? Rol.valueOf(request.getRol().toUpperCase()) : Rol.CLIENTE;
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "Rol inválido. Usá ADMIN, EMPLEADO o CLIENTE.", req);
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setNombre(request.getNombre());
        nuevo.setApellido(request.getApellido());
        nuevo.setEmail(request.getEmail());
        nuevo.setRol(rol);
        usuarioRepository.save(nuevo);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado con rol " + rol);
    }

    @PostMapping("/registro-cliente")
    public ResponseEntity<?> registroCliente(@Valid @RequestBody RegisterRequest request, HttpServletRequest req) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return error(HttpStatus.CONFLICT, "El usuario ya existe", req);
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setNombre(request.getNombre());
        nuevo.setApellido(request.getApellido());
        nuevo.setEmail(request.getEmail());
        nuevo.setRol(Rol.CLIENTE);
        usuarioRepository.save(nuevo);

        return ResponseEntity.ok(armarRespuesta(nuevo));
    }

    @GetMapping("/usuarios/{username}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable String username) {
        return usuarioRepository.findByUsername(username)
                .map(u -> ResponseEntity.ok(UsuarioDTO.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll().stream()
                .map(AdminUsuarioDTO::from)
                .toList());
    }

    @PatchMapping("/usuarios/{username}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String username,
                                            @Valid @RequestBody ActualizarEstadoUsuarioRequest request,
                                            HttpServletRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return error(HttpStatus.NOT_FOUND, "No existe un usuario con username '" + username + "'", req);
        }

        usuario.setHabilitado(request.habilitado());
        usuarioRepository.save(usuario);

        if (!request.habilitado()) {
            refreshTokenService.revocarTodosDe(usuario);
        }

        return ResponseEntity.ok(AdminUsuarioDTO.from(usuario));
    }

    @PutMapping("/usuarios/{username}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String username,
                                                @Valid @RequestBody ActualizarUsuarioRequest request,
                                                HttpServletRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return error(HttpStatus.NOT_FOUND, "No existe un usuario con username '" + username + "'", req);
        }

        if (request.getRol() != null && !request.getRol().isBlank()) {
            try {
                usuario.setRol(Rol.valueOf(request.getRol().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return error(HttpStatus.BAD_REQUEST, "Rol inválido. Usá ADMIN, EMPLEADO o CLIENTE.", req);
            }
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(AdminUsuarioDTO.from(usuario));
    }

    private boolean estaHabilitado(Usuario usuario) {
        return !Boolean.FALSE.equals(usuario.getHabilitado());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest req) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    private LoginResponse armarRespuesta(Usuario usuario) {
        String accessToken = jwtService.generarToken(usuario);
        RefreshToken refreshToken = refreshTokenService.crear(usuario);

        return new LoginResponse(
                accessToken,
                "Bearer",
                refreshToken.getToken(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol().name(),
                jwtService.getExpirationMs() / 1000
        );
    }
}
