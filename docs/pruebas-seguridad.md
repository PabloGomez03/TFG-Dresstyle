# Pruebas de seguridad (401 / 403)

Estas pruebas validan que la API rechaza correctamente peticiones no autorizadas. Todas se ejecutan contra el **API Gateway** (`http://localhost:8080`), igual que el frontend.

## Objetivo

Comprobar tres escenarios de seguridad:

| # | Escenario | Código HTTP esperado |
|---|-----------|----------------------|
| 1 | Petición sin token a ruta protegida | **401 Unauthorized** |
| 2 | Petición con token inválido | **401 Unauthorized** |
| 3 | Petición de cliente a endpoint de administrador | **403 Forbidden** |

## Requisitos

1. Stack en marcha: `docker compose up -d`
2. Gateway accesible en el puerto **8080**
3. **Windows PowerShell 5.1+** o PowerShell 7+

Comprueba que el gateway está activo:

```powershell
docker ps --filter name=dresstyle-gateway
Test-NetConnection localhost -Port 8080
```

## Ejecución automática (recomendada)

Script: [`scripts/security-tests.ps1`](../scripts/security-tests.ps1)

```powershell
cd C:\Users\pablo\Desktop\TFG-Dresstyle
.\scripts\security-tests.ps1
```

### Salida esperada

```
=== Pruebas de seguridad Dresstyle ===
API: http://localhost:8080
PowerShell: 5.1.xxxxx

[PASS] 1. Peticion sin token (GET /api/auth/profile)
       Esperado: 401 | Obtenido: 401
[PASS] 2. Peticion con token invalido (GET /api/auth/profile)
       Esperado: 401 | Obtenido: 401
Usuario cliente de prueba listo (login): security-test-client@dresstyle.test
[PASS] 3. Cliente en endpoint admin (POST /api/catalog/products)
       Esperado: 403 | Obtenido: 403

[PASS] Control: admin puede crear producto (HTTP 201)

Resumen: 3 / 3 pruebas obligatorias correctas
```

### Variables de entorno opcionales

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `DRESSTYLE_API_URL` | `http://localhost:8080` | URL base del API Gateway |
| `ADMIN_EMAIL` | `admin@admin.com` | Email del administrador |
| `ADMIN_PASSWORD` | `admin` | Contraseña del administrador |
| `TEST_CLIENT_EMAIL` | `security-test-client@dresstyle.test` | Email del cliente de prueba |
| `TEST_CLIENT_PASSWORD` | `password123` | Contraseña del cliente de prueba |

Ejemplo:

```powershell
$env:ADMIN_PASSWORD = "admin"
.\scripts\security-tests.ps1
```

### Notas del script

- Compatible con **Windows PowerShell 5.1** (no requiere PowerShell 7).
- Si el usuario cliente ya existe de una ejecución anterior, el script hace **login** en lugar de volver a registrar.
- El producto de prueba lleva un nombre único por ejecución (evita conflictos en el control del admin).
- Incluye un **control opcional**: el admin debe poder crear el producto (HTTP **201**).

## Pruebas manuales

Sustituye `TOKEN_CLIENTE` por el JWT obtenido con login de un usuario con rol `ROLE_CLIENT`.

### 1. Petición sin token → 401

Ruta protegida: perfil de usuario.

```bash
curl -i -X GET "http://localhost:8080/api/auth/profile"
```

**Esperado:** `HTTP/1.1 401 Unauthorized`

### 2. Petición con token inválido → 401

```bash
curl -i -X GET "http://localhost:8080/api/auth/profile" \
  -H "Authorization: Bearer token.invalido.de.prueba"
```

**Esperado:** `HTTP/1.1 401 Unauthorized`

### 3. Cliente en endpoint de administrador → 403

Solo usuarios con `ROLE_ADMIN` pueden crear productos (`POST /api/catalog/products`).

```bash
curl -i -X POST "http://localhost:8080/api/catalog/products" \
  -H "Authorization: Bearer TOKEN_CLIENTE" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test\",\"description\":\"Test\",\"imageUrl\":\"https://example.com/x.jpg\",\"price\":9.99,\"stock\":1,\"category\":\"Camisetas\"}"
```

**Esperado:** `HTTP/1.1 403 Forbidden`

#### Obtener token de cliente

```bash
curl -s -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Cliente Test\",\"email\":\"cliente@test.com\",\"password\":\"password123\"}"

curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"cliente@test.com\",\"password\":\"password123\"}"
```

Copia el campo `token` de la respuesta JSON.

#### Control (opcional): admin → 201

```bash
curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@admin.com\",\"password\":\"admin\"}"
```

Con el token de admin, el mismo `POST /api/catalog/products` debe devolver **201 Created**.

## Pruebas manuales en PowerShell

```powershell
# 1. Sin token (401)
try {
    Invoke-WebRequest -Uri "http://localhost:8080/api/auth/profile" -UseBasicParsing
} catch {
    [int]$_.Exception.Response.StatusCode  # debe ser 401
}

# 2. Token invalido (401)
try {
    Invoke-WebRequest -Uri "http://localhost:8080/api/auth/profile" `
        -Headers @{ Authorization = "Bearer token.invalido" } -UseBasicParsing
} catch {
    [int]$_.Exception.Response.StatusCode  # debe ser 401
}
```

Para la prueba 3, usa el script automático o el flujo curl anterior.

## Dónde se aplica la seguridad

| Capa | Comportamiento |
|------|----------------|
| **API Gateway** | Exige JWT en rutas `/api/**` (salvo login, register y GET públicos de catálogo/planes). |
| **Catalog Service** | `POST`, `PUT` y `DELETE` de productos exigen `ROLE_ADMIN` en el claim `roles` del JWT. |

## Solución de problemas

| Problema | Posible causa | Qué hacer |
|----------|---------------|-----------|
| `El puerto 8080 no responde` | Docker no levantado | `docker compose up -d` |
| Error al registrar cliente en prueba 3 | Usuario ya existe | Normal: el script reintenta con login |
| Script falla en PS 5.1 con parámetro desconocido | Versión antigua del script | Usa la versión actual de `security-tests.ps1` |
| Prueba 3 devuelve 401 en lugar de 403 | Token no enviado o roles no mapeados | Verifica JWT y `SecurityConfig` del catalog service |

## Evidencias para la memoria del TFG

Para cada prueba, guarda captura o texto con:

- URL y método HTTP
- Cabeceras enviadas (enmascara el token: `Bearer eyJ...xxx`)
- Código de estado HTTP obtenido
- Fragmento del cuerpo de respuesta si aplica

## Tabla resumen

| # | Escenario | Petición | Código esperado |
|---|-----------|----------|-----------------|
| 1 | Sin autenticación | `GET /api/auth/profile` sin `Authorization` | **401** |
| 2 | Token inválido | `GET /api/auth/profile` con `Bearer` corrupto | **401** |
| 3 | Cliente → recurso admin | `POST /api/catalog/products` con JWT de `ROLE_CLIENT` | **403** |
