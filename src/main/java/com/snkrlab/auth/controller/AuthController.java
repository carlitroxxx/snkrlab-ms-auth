package com.snkrlab.auth.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.snkrlab.auth.dto.PerfilUsuarioDTO;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @SuppressWarnings("unchecked")
    @GetMapping("/perfil")
    public PerfilUsuarioDTO perfil(@AuthenticationPrincipal Jwt jwt) {
        String id = jwt.getClaimAsString("oid");
        String nombre = jwt.getClaimAsString("name");
        String email = jwt.hasClaim("preferred_username")
                ? jwt.getClaimAsString("preferred_username")
                : jwt.getClaimAsString("email");

        List<String> roles = jwt.hasClaim("roles")
                ? (List<String>) jwt.getClaim("roles")
                : Collections.emptyList();

        return new PerfilUsuarioDTO(id, nombre, email, roles);
    }

    @GetMapping("/public")
    public String publico() {
        return "ms-auth activo. Este endpoint no requiere token.";
    }
}