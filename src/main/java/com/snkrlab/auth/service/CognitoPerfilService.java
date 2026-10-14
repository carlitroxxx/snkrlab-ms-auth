package com.snkrlab.auth.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.snkrlab.auth.dto.PerfilUsuarioDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CognitoPerfilService {

    private final RestClient restClient;

    public CognitoPerfilService(
            @Value("${app.seguridad.cognito-domain}") String dominio) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(dominio)
                .requestFactory(requestFactory)
                .build();
    }

    public PerfilUsuarioDTO obtenerPerfil(Jwt jwt) {
        Map<String, Object> datos;

        try {
            datos = restClient.get()
                    .uri("/oauth2/userInfo")
                    .headers(headers ->
                            headers.setBearerAuth(jwt.getTokenValue())
                    )
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    Map<String, Object>>() {}
                    );
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "La sesión de Cognito no es válida. Inicia sesión nuevamente."
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Cognito no pudo devolver los datos del perfil."
            );
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo conectar con Cognito para consultar el perfil."
            );
        }

        String usuarioId = jwt.getSubject();

        if (datos == null
                || usuarioId == null
                || usuarioId.isBlank()
                || !usuarioId.equals(datos.get("sub"))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "La respuesta de Cognito no corresponde al usuario."
            );
        }

        String nombre = obtenerTexto(datos, "name");
        String email = obtenerTexto(datos, "email");

        if (nombre == null || nombre.isBlank()) {
            nombre = obtenerTexto(datos, "nickname");
        }

        if (nombre == null || nombre.isBlank()) {
            nombre = email;
        }

        List<String> grupos =
                jwt.getClaimAsStringList("cognito:groups");

        // Por ahora, Cognito se utiliza para clientes externos.
        List<String> roles =
                grupos != null && grupos.contains("Cliente")
                        ? List.of("Cliente")
                        : List.of();

        return new PerfilUsuarioDTO(
                "cognito:" + usuarioId,
                nombre,
                email,
                roles
        );
    }

    private String obtenerTexto(
            Map<String, Object> datos,
            String atributo) {

        Object valor = datos.get(atributo);

        return valor instanceof String ? (String) valor : null;
    }
}