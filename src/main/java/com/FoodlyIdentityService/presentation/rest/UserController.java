package com.FoodlyIdentityService.presentation.rest;

import com.FoodlyIdentityService.application.dto.UserProfileDto;
import com.FoodlyIdentityService.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Endpoints protegidos para gestión del perfil de usuario")
@SecurityScheme(
    name               = "BearerAuth",
    type               = SecuritySchemeType.HTTP,
    scheme             = "bearer",
    bearerFormat       = "JWT",
    description        = "JWT obtenido de POST /api/auth/login. Pegar el accessToken aquí."
)
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    @Operation(
        summary     = "Obtener mi perfil",
        description = "Retorna los datos del perfil del usuario actualmente autenticado. " +
                      "Requiere el JWT en el header: `Authorization: Bearer <token>`. " +
                      "El userId se extrae del claim 'sub' del token."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "Perfil del usuario autenticado",
            content      = @Content(schema = @Schema(implementation = UserProfileDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o expirado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado (raro si el JWT es válido)")
    })
    public ResponseEntity<UserProfileDto> getMyProfile(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.debug("[UserController] GET /api/users/me | userId={}", userId);
        UserProfileDto profile = authService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
