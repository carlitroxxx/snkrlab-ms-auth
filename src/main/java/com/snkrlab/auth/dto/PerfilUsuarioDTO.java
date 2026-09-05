package com.snkrlab.auth.dto;

import java.util.List;

public record PerfilUsuarioDTO(
        String id,
        String nombre,
        String email,
        List<String> roles
) {
}