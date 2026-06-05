package com.FoodlyIdentityService.presentation.rest;

import com.FoodlyIdentityService.application.dto.AuthResponseDto;
import com.FoodlyIdentityService.application.dto.LoginRequestDto;
import com.FoodlyIdentityService.application.dto.RegisterRequestDto;
import com.FoodlyIdentityService.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos de registro e inicio de sesión en Foodly")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
        summary     = "Registrar nuevo usuario",
        description = "Crea una cuenta nueva en la plataforma Foodly. " +
                      "Hashea la contraseña con BCrypt, persiste en MySQL y " +
                      "publica el evento USER_CREATED en ActiveMQ. " +
                      "Retorna un JWT firmado listo para usar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description  = "Usuario registrado exitosamente. Retorna JWT.",
            content      = @Content(schema = @Schema(implementation = AuthResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Bean Validation)"),
        @ApiResponse(responseCode = "409", description = "Email o username ya registrado")
    })
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("[AuthController] POST /api/auth/register | email={}", request.getEmail());
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
        summary     = "Iniciar sesión",
        description = "Autentica un usuario con email y contraseña. " +
                      "Verifica el hash BCrypt y emite un JWT firmado HS256 " +
                      "con los claims: sub (userId), email, roles, iat, exp."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "Login exitoso. Retorna JWT Bearer token.",
            content      = @Content(schema = @Schema(implementation = AuthResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta inactiva")
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("[AuthController] POST /api/auth/login | email={}", request.getEmail());
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
