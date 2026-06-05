<#
.SYNOPSIS
    Pruebas de seguridad de la API Dresstyle (401 / 403).

.DESCRIPTION
    1. Peticion sin token a ruta protegida        -> esperado 401
    2. Peticion con token invalido                -> esperado 401
    3. Cliente en endpoint de administrador       -> esperado 403

    Requisito: docker compose up -d (gateway en http://localhost:8080)

.EXAMPLE
    .\scripts\security-tests.ps1
#>

$ErrorActionPreference = "Stop"

$BaseUrl = if ($env:DRESSTYLE_API_URL) { $env:DRESSTYLE_API_URL.TrimEnd("/") } else { "http://localhost:8080" }

$AdminEmail = if ($env:ADMIN_EMAIL) { $env:ADMIN_EMAIL } else { "admin@admin.com" }
$AdminPassword = if ($env:ADMIN_PASSWORD) { $env:ADMIN_PASSWORD } else { "admin" }

$ClientEmail = if ($env:TEST_CLIENT_EMAIL) { $env:TEST_CLIENT_EMAIL } else { "security-test-client@dresstyle.test" }
$ClientPassword = if ($env:TEST_CLIENT_PASSWORD) { $env:TEST_CLIENT_PASSWORD } else { "password123" }
$ClientName = "Security Test Client"

function Get-HttpStatusFromError {
    param($ErrorRecord)
    if ($ErrorRecord.Exception.Response) {
        return [int]$ErrorRecord.Exception.Response.StatusCode.value__
    }
    return $null
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token = $null,
        [object]$Body = $null
    )

    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $webRequestParams = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
    }

    if ($null -ne $Body) {
        $webRequestParams["Body"] = ($Body | ConvertTo-Json -Depth 5)
    }

    try {
        $response = Invoke-WebRequest @webRequestParams
        return [int]$response.StatusCode
    } catch {
        $status = Get-HttpStatusFromError -ErrorRecord $_
        if ($null -ne $status) {
            return $status
        }
        throw
    }
}

function Invoke-ApiWithContent {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token = $null,
        [object]$Body = $null
    )

    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $webRequestParams = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
    }

    if ($null -ne $Body) {
        $webRequestParams["Body"] = ($Body | ConvertTo-Json -Depth 5)
    }

    try {
        $response = Invoke-WebRequest @webRequestParams
        return @{
            StatusCode = [int]$response.StatusCode
            Content = $response.Content
        }
    } catch {
        $status = Get-HttpStatusFromError -ErrorRecord $_
        $content = $null

        if ($_.Exception.Response) {
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $content = $reader.ReadToEnd()
                    $reader.Close()
                }
            } catch {
                $content = $null
            }
        }

        if ($null -ne $status) {
            return @{
                StatusCode = $status
                Content = $content
            }
        }

        throw
    }
}

function Write-Result {
    param(
        [string]$Name,
        [int]$Expected,
        [int]$Actual,
        [string]$Detail = ""
    )

    $ok = ($Actual -eq $Expected)
    $label = if ($ok) { "PASS" } else { "FAIL" }
    $color = if ($ok) { "Green" } else { "Red" }

    Write-Host "[$label] $Name" -ForegroundColor $color
    Write-Host "       Esperado: $Expected | Obtenido: $Actual"
    if ($Detail) {
        Write-Host "       $Detail"
    }

    return $ok
}

function Get-Token {
    param(
        [string]$Email,
        [string]$Password
    )

    $response = Invoke-ApiWithContent -Method POST -Path "/api/auth/login" -Body @{
        email = $Email
        password = $Password
    }

    if ($response.StatusCode -ne 200) {
        throw "Login fallido para $Email (HTTP $($response.StatusCode))"
    }

    $data = $response.Content | ConvertFrom-Json
    if (-not $data.token) {
        throw "Login sin token para $Email"
    }

    return $data.token
}

function Ensure-ClientUser {
    $credentials = @{
        email = $ClientEmail
        password = $ClientPassword
    }

    $login = Invoke-ApiWithContent -Method POST -Path "/api/auth/login" -Body $credentials
    if ($login.StatusCode -eq 200) {
        Write-Host "Usuario cliente de prueba listo (login): $ClientEmail"
        return
    }

    $register = Invoke-ApiWithContent -Method POST -Path "/api/auth/register" -Body @{
        name = $ClientName
        email = $ClientEmail
        password = $ClientPassword
    }

    if ($register.StatusCode -in 200, 201) {
        Write-Host "Usuario cliente de prueba registrado: $ClientEmail"
        return
    }

    if ($register.StatusCode -in 409, 400) {
        Write-Host "El email ya estaba registrado; se reutiliza el usuario."
    } else {
        Write-Host "Register devolvio HTTP $($register.StatusCode); se intenta login."
    }

    $loginRetry = Invoke-ApiWithContent -Method POST -Path "/api/auth/login" -Body $credentials
    if ($loginRetry.StatusCode -eq 200) {
        Write-Host "Usuario cliente de prueba listo (login tras register): $ClientEmail"
        return
    }

    throw "No se pudo preparar el cliente de prueba (register=$($register.StatusCode), login=$($loginRetry.StatusCode))"
}

function Test-GatewayReachable {
    try {
        $tcp = Test-NetConnection -ComputerName "localhost" -Port 8080 -WarningAction SilentlyContinue -ErrorAction Stop
        return $tcp.TcpTestSucceeded
    } catch {
        return $false
    }
}

# --- Ejecucion ---

Write-Host ""
Write-Host "=== Pruebas de seguridad Dresstyle ===" -ForegroundColor Cyan
Write-Host "API: $BaseUrl"
Write-Host "PowerShell: $($PSVersionTable.PSVersion)"
Write-Host ""

if (-not (Test-GatewayReachable)) {
    Write-Host "ERROR: El puerto 8080 no responde en localhost." -ForegroundColor Red
    Write-Host "Levanta el stack: docker compose up -d"
    Write-Host "Comprueba: docker ps --filter name=dresstyle-gateway"
    exit 1
}

$passed = 0
$total = 3

# 1) Sin token -> 401
$status1 = Invoke-Api -Method GET -Path "/api/auth/profile"
if (Write-Result -Name "1. Peticion sin token (GET /api/auth/profile)" -Expected 401 -Actual $status1) {
    $passed++
}

# 2) Token invalido -> 401
$status2 = Invoke-Api -Method GET -Path "/api/auth/profile" -Token "token.invalido.de.prueba"
if (Write-Result -Name "2. Peticion con token invalido (GET /api/auth/profile)" -Expected 401 -Actual $status2) {
    $passed++
}

# 3) Cliente en endpoint admin -> 403
Ensure-ClientUser
$clientToken = Get-Token -Email $ClientEmail -Password $ClientPassword

$productSuffix = Get-Date -Format "yyyyMMddHHmmss"
$productBody = @{
    name = "Producto prueba seguridad $productSuffix"
    description = "Test automatizado de seguridad"
    imageUrl = "https://example.com/img.jpg"
    price = 9.99
    stock = 1
    category = "Camisetas"
}

$status3 = Invoke-Api -Method POST -Path "/api/catalog/products" -Token $clientToken -Body $productBody
if (Write-Result -Name "3. Cliente en endpoint admin (POST /api/catalog/products)" -Expected 403 -Actual $status3 -Detail "Usuario: $ClientEmail") {
    $passed++
}

# Control opcional: admin debe poder crear (201)
try {
    $adminToken = Get-Token -Email $AdminEmail -Password $AdminPassword
    $adminStatus = Invoke-Api -Method POST -Path "/api/catalog/products" -Token $adminToken -Body $productBody
    $adminOk = ($adminStatus -in 200, 201)
    $adminLabel = if ($adminOk) { "PASS" } else { "INFO" }
    $adminColor = if ($adminOk) { "Green" } else { "Yellow" }
    Write-Host ""
    Write-Host "[$adminLabel] Control: admin puede crear producto (HTTP $adminStatus)" -ForegroundColor $adminColor
} catch {
    Write-Host ""
    Write-Host "[INFO] Control admin omitido: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Resumen: $passed / $total pruebas obligatorias correctas" -ForegroundColor $(if ($passed -eq $total) { "Green" } else { "Red" })
Write-Host ""

if ($passed -ne $total) {
    exit 1
}
