package com.snkrlab.auth.controller;

import java.util.List;

import com.snkrlab.auth.dto.PerfilUsuarioDTO;
import com.snkrlab.auth.service.CognitoPerfilService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CognitoPerfilService cognitoPerfilService;
    private final String cognitoIssuer;

    public AuthController(
            CognitoPerfilService cognitoPerfilService,
            @Value("${app.seguridad.cognito-issuer}") String cognitoIssuer) {

        this.cognitoPerfilService = cognitoPerfilService;
        this.cognitoIssuer = cognitoIssuer;
    }

    @GetMapping("/perfil")
    public PerfilUsuarioDTO perfil(@AuthenticationPrincipal Jwt jwt) {
        String emisor = jwt.getClaimAsString("iss");

        if (cognitoIssuer.equals(emisor)) {
            return cognitoPerfilService.obtenerPerfil(jwt);
        }

        String id = jwt.getClaimAsString("oid");
        String nombre = jwt.getClaimAsString("name");

        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("preferred_username");
        }

        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("upn");
        }

        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null) {
            roles = List.of();
        }

        return new PerfilUsuarioDTO(id, nombre, email, roles);
    }

    @GetMapping("/public")
    public String publico() {
        return "ms-auth activo. Este endpoint no requiere token.";
    }
}