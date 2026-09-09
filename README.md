# snkrlab-ms-auth

Microservicio de autenticación del sistema **SNKRLAB**. No mantiene usuarios propios: valida los tokens JWT emitidos por **Azure AD (Microsoft Entra ID)** y expone la información del usuario autenticado a partir de los claims del token.

## Herramientas

- Java 21 · Spring Boot 4.1.1
- Spring Security + OAuth2 Resource Server (validación de JWT contra Azure AD)
- Lombok
- Docker

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `AZURE_ISSUER_URI` | Base del issuer de Azure AD | `https://login.microsoftonline.com/` |
| `AZURE_TENANT_ID` | Tenant ID de Azure AD | `db9d1cc0-8c32-4341-bc72-c348daf096fb` |

## Ejecutar en local

```bash
./mvnw spring-boot:run
```

Corre en `http://localhost:8081`.

## Ejecutar con Docker

```bash
docker build -t ms-auth:latest .
docker run -d --name ms-auth \
  -p 8081:8081 \
  -e AZURE_TENANT_ID="<tenant-id>" \
  ms-auth:latest
```

## Endpoints

Base path: `/api/v1/auth`

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/public` | No | Endpoint de prueba, confirma que el servicio está activo |
| GET | `/perfil` | Sí (Bearer JWT) | Devuelve los datos del usuario autenticado extraídos del token: `oid`, `name`, `preferred_username`/`email` y `roles` |

### Ejemplo `GET /api/v1/perfil`

```json
{
  "id": "5f2c...-oid",
  "nombre": "Nombre Apellido",
  "email": "usuario@dominio.com",
  "roles": ["Cliente"]
}
```

## Seguridad

- CORS habilitado solo para `http://localhost:5173` (ajustar en `SecurityConfig` según el origen real del frontend desplegado).
- Todas las rutas requieren JWT válido excepto `/api/v1/auth/public`.

## Despliegue

Se despliega en una instancia EC2 (contenedor Docker) y quedar expuesto mediante rutas explícitas de AWS API Gateway (`GET /api/v1/auth/public`, `GET /api/v1/auth/perfil`).
