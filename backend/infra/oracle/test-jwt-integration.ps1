#requires -Version 5.1

$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
$envFile = Join-Path $PSScriptRoot '.env'
$runId = [Guid]::NewGuid().ToString('N')
$issuer = 'http://localhost:8184'
$ports = @(8181, 8183, 8184, 8185, 8186)
$script:services = @()
$script:logFiles = @()
$script:results = @()
$script:userId = $null
$script:bootstrapAdminId = $null
$script:csrfContextHashes = @()
$script:catalogProductId = $null
$script:failure = $null
$logDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "gamerzone-jwt-$runId"
$catalogCategory = "E2E-$runId"
$catalogUpdatedCategory = "E2E-UPDATED-$runId"
$catalogDeveloper = "E2E Dev $runId"
$catalogPublisher = "E2E Pub $runId"
$catalogMode = "E2E Mode $runId"
$catalogLanguage = "E2E Language $runId"

if (-not (Test-Path $envFile)) {
    throw 'Copy .env.example to .env and configure the local Oracle test password first.'
}

$environment = @{}
foreach ($line in Get-Content $envFile) {
    if ($line -match '^\s*([^#\s][^=]*)=(.*)$') {
        $environment[$matches[1].Trim()] = $matches[2].Trim()
    }
}
$schemaPassword = $environment['TEST_SCHEMA_PASSWORD']
if ([string]::IsNullOrWhiteSpace($schemaPassword) -or
        $schemaPassword -eq 'replace_with_another_local_test_password') {
    throw 'TEST_SCHEMA_PASSWORD is not configured in the ignored Oracle .env file.'
}

$containerState = & docker inspect --format '{{.State.Running}}' gamerzone-oracle-test
if ($LASTEXITCODE -ne 0 -or $containerState -ne 'true') {
    throw 'Start the isolated Oracle test container before running this test.'
}

foreach ($port in $ports) {
    if (@(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue).Count -gt 0) {
        throw "Port $port is already in use; the test will not stop or replace its process."
    }
}

$keyJson = & node -e "const c=require('crypto');const p=c.generateKeyPairSync('rsa',{modulusLength:2048,privateKeyEncoding:{type:'pkcs8',format:'pem'},publicKeyEncoding:{type:'spki',format:'pem'}});process.stdout.write(JSON.stringify(p));"
if ($LASTEXITCODE -ne 0) {
    throw 'Could not generate an ephemeral RSA test key; Node.js is required.'
}
$keyPair = $keyJson | ConvertFrom-Json
$privateKey = $keyPair.privateKey -replace "`r?`n", '\n'
$publicKey = $keyPair.publicKey -replace "`r?`n", '\n'
$keyId = "gamerzone-e2e-$($runId.Substring(0, 8))"
$previousKeyJson = & node -e "const c=require('crypto');const p=c.generateKeyPairSync('rsa',{modulusLength:2048,privateKeyEncoding:{type:'pkcs8',format:'pem'},publicKeyEncoding:{type:'spki',format:'pem'}});process.stdout.write(JSON.stringify(p));"
if ($LASTEXITCODE -ne 0) {
    throw 'Could not generate an ephemeral previous RSA test key; Node.js is required.'
}
$previousKeyPair = $previousKeyJson | ConvertFrom-Json
$previousPublicKey = $previousKeyPair.publicKey -replace "`r?`n", '\n'
$previousKeyId = "gamerzone-e2e-previous-$($runId.Substring(0, 8))"
$previousPublicKeysJson = ConvertTo-Json -InputObject @(@{
        kid = $previousKeyId
        pem = $previousPublicKey
    }) -Compress
$bootstrapAdminEmail = "bootstrap-$runId@gmail.com"
$bootstrapAdminPassword = 'BootstrapE2E26!Secure'
$cartQuoteSecret = [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
New-Item -ItemType Directory -Path $logDirectory | Out-Null

function Start-TestService {
    param(
        [string]$Name,
        [string]$Schema,
        [int]$Port,
        [string]$Audience
    )

    $modulePath = Join-Path $repositoryRoot "backend\$Name"
    $env:SERVER_PORT = [string]$Port
    $env:DB_URL = 'jdbc:oracle:thin:@//localhost:1522/XEPDB1'
    $env:DB_USERNAME = $Schema
    $env:DB_PASSWORD = $schemaPassword
    $env:DB_SCHEMA = $Schema
    $env:JWT_ISSUER_URI = $issuer
    $env:JWT_JWKS_URI = "$issuer/.well-known/jwks.json"
    $env:JWT_PRIVATE_KEY = $privateKey
    $env:JWT_PUBLIC_KEY = $publicKey
    $env:JWT_KEY_ID = $keyId
    $env:JWT_PREVIOUS_PUBLIC_KEYS_JSON = $previousPublicKeysJson
    $env:JWT_AUDIENCES = 'ms-catalogo,ms-carrito,ms-contacto,ms-pedido'
    $env:JWT_AUDIENCE = $Audience
    $env:CART_QUOTE_HMAC_SECRET = $cartQuoteSecret
    $env:CONTACT_SERVICE_URL = 'http://localhost:8185'
    if ($Name -eq 'ms-auth') {
        $env:BOOTSTRAP_ADMIN_EMAIL = $bootstrapAdminEmail
        $env:BOOTSTRAP_ADMIN_PASSWORD = $bootstrapAdminPassword
    } else {
        Remove-Item Env:BOOTSTRAP_ADMIN_EMAIL -ErrorAction SilentlyContinue
        Remove-Item Env:BOOTSTRAP_ADMIN_PASSWORD -ErrorAction SilentlyContinue
    }
    Remove-Item Env:AUTH_ADMIN_RECOVERY_EMAIL -ErrorAction SilentlyContinue
    Remove-Item Env:AUTH_ADMIN_RECOVERY_PASSWORD -ErrorAction SilentlyContinue
    $env:MAVEN_OPTS = '-Xmx512m'

    $stdout = Join-Path $logDirectory "$Name.out.log"
    $stderr = Join-Path $logDirectory "$Name.err.log"
    $script:logFiles += @($stdout, $stderr)
    $process = Start-Process -FilePath $env:ComSpec `
        -ArgumentList @('/c', 'mvnw.cmd -q -Dspring-boot.run.fork=false spring-boot:run') `
        -WorkingDirectory $modulePath `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -PassThru
    $service = [pscustomobject]@{
        Name = $Name
        Port = $Port
        Process = $process
        Stdout = $stdout
        Stderr = $stderr
    }
    $script:services += ,$service

    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        if (@(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue).Count -gt 0) {
            return $service
        }
        Start-Sleep -Seconds 2
    }
    throw "$Name did not start listening on port $Port."
}

function Stop-TestService {
    param([pscustomobject]$Service)

    $owners = @(Get-NetTCPConnection -LocalPort $Service.Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique)
    foreach ($owner in $owners) {
        if ([int]$owner -gt 0 -and [int]$owner -ne $PID) {
            Stop-Process -Id ([int]$owner) -Force -ErrorAction SilentlyContinue
        }
    }
    if ($Service.Process -and (Get-Process -Id $Service.Process.Id -ErrorAction SilentlyContinue)) {
        Stop-Process -Id $Service.Process.Id -Force -ErrorAction SilentlyContinue
    }
}

function Get-HttpStatus {
    param(
        [string]$Uri,
        [AllowNull()]
        [string]$Token,
        [string]$Method = 'Get',
        [AllowNull()]
        [string]$Body = $null
    )

    try {
        $request = @{
            Method = $Method
            Uri = $Uri
            UseBasicParsing = $true
            TimeoutSec = 20
        }
        if (-not [string]::IsNullOrWhiteSpace($Token)) {
            $request.Headers = @{ Authorization = "Bearer $Token" }
        }
        if (-not [string]::IsNullOrWhiteSpace($Body)) {
            $request.ContentType = 'application/json'
            $request.Body = $Body
        }
        $response = Invoke-WebRequest @request
        return [int]$response.StatusCode
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            return [int]$_.Exception.Response.StatusCode
        }
        throw "HTTP request to $Uri failed without an HTTP response: $($_.Exception.Message)"
    }
}

function Get-AuthHttpStatus {
    param(
        [string]$Uri,
        [string]$Method,
        [Microsoft.PowerShell.Commands.WebRequestSession]$Session,
        [hashtable]$Headers,
        [AllowNull()]
        [string]$Body
    )

    try {
        $request = @{
            Method = $Method
            Uri = $Uri
            Headers = $Headers
            WebSession = $Session
            UseBasicParsing = $true
            TimeoutSec = 20
        }
        if ($null -ne $Body) {
            $request.ContentType = 'application/json'
            $request.Body = $Body
        }
        $response = Invoke-WebRequest @request
        return [int]$response.StatusCode
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            return [int]$_.Exception.Response.StatusCode
        }
        throw 'Auth API request failed without an HTTP response.'
    }
}

function Get-AuthCookieValue {
    param(
        [Microsoft.PowerShell.Commands.WebRequestSession]$Session,
        [string]$Name
    )

    $cookies = $Session.Cookies.GetCookies([Uri]"$issuer/api/v1/auth/refresh")
    $cookie = $cookies[$Name]
    if ($null -eq $cookie) {
        return $null
    }
    return [string]$cookie.Value
}

function Add-CsrfContextForCleanup {
    param([Microsoft.PowerShell.Commands.WebRequestSession]$Session)

    $context = Get-AuthCookieValue -Session $Session -Name 'gz_csrf_context'
    if ([string]::IsNullOrWhiteSpace($context)) {
        return
    }
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        $hash = [BitConverter]::ToString(
            $sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($context))).Replace('-', '').ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
    if ($script:csrfContextHashes -notcontains $hash) {
        $script:csrfContextHashes += $hash
    }
}

function Assert-Status {
    param(
        [string]$Name,
        [int]$Expected,
        [int]$Actual
    )

    if ($Actual -ne $Expected) {
        throw "$Name returned HTTP $Actual; expected HTTP $Expected."
    }
    $script:results += "$Name`: HTTP $Actual"
}

function Invoke-OracleSql {
    param([string]$Sql)

    $output = $Sql | & docker exec -i gamerzone-oracle-test bash -lc 'sqlplus -s / as sysdba'
    if ($LASTEXITCODE -ne 0) {
        throw 'Oracle test setup or cleanup SQL failed.'
    }
    return $output
}

function New-TestRut {
    $digits = [string](Get-Random -Minimum 10000000 -Maximum 99999999)
    $weight = 2
    $sum = 0
    for ($index = $digits.Length - 1; $index -ge 0; $index--) {
        $sum += ([int]::Parse($digits.Substring($index, 1)) * $weight)
        $weight++
        if ($weight -gt 7) { $weight = 2 }
    }
    $checkDigit = 11 - ($sum % 11)
    if ($checkDigit -eq 11) { $verifier = '0' }
    elseif ($checkDigit -eq 10) { $verifier = 'K' }
    else { $verifier = [string]$checkDigit }
    return "$digits-$verifier"
}

function Get-JwtClaims {
    param([string]$Token)

    $parts = $Token.Split('.')
    if ($parts.Length -ne 3) { throw 'ms-auth returned a malformed JWT.' }
    $payload = $parts[1].Replace('-', '+').Replace('_', '/')
    $payload += '=' * ((4 - ($payload.Length % 4)) % 4)
    return [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload)) | ConvertFrom-Json
}

function New-TestJwt {
    param(
        [string]$Subject,
        [string]$TokenIssuer = $issuer,
        [string[]]$Audiences = @('ms-catalogo', 'ms-carrito', 'ms-contacto', 'ms-pedido'),
        [long]$ExpirationOffsetSeconds = 600,
        [object]$Roles = @('CLIENTE'),
        [switch]$OmitRoles,
        [string]$SigningPrivateKey = $keyPair.privateKey,
        [string]$SigningKeyId = $keyId
    )
    $now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    $claims = @{
        iss = $TokenIssuer
        sub = $Subject
        aud = $Audiences
        iat = $now
        exp = $now + $ExpirationOffsetSeconds
        jti = [Guid]::NewGuid().ToString()
    }
    if (-not $OmitRoles) {
        $claims.roles = $Roles
    }
    $fixture = @{
        privateKey = $SigningPrivateKey
        header = @{ alg = 'RS256'; typ = 'JWT'; kid = $SigningKeyId }
        claims = $claims
    } | ConvertTo-Json -Depth 8 -Compress
    $token = $fixture | & node -e "let s='';process.stdin.on('data',d=>s+=d);process.stdin.on('end',()=>{const c=JSON.parse(s);const b=v=>Buffer.from(JSON.stringify(v)).toString('base64url');const i=b(c.header)+'.'+b(c.claims);const sig=require('crypto').sign('RSA-SHA256',Buffer.from(i),c.privateKey).toString('base64url');process.stdout.write(i+'.'+sig)});"
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($token)) {
        throw 'Could not create a signed negative-test JWT fixture.'
    }
    return [string]$token
}

try {
    $auth = Start-TestService -Name 'ms-auth' -Schema 'GZ_TEST_AUTH' -Port 8184 -Audience 'ms-auth'
    $jwks = Invoke-RestMethod -Uri "$issuer/.well-known/jwks.json" -TimeoutSec 15
    $publishedKeyIds = @($jwks.keys | ForEach-Object { $_.kid })
    if ($publishedKeyIds.Count -ne 2 -or $publishedKeyIds -notcontains $keyId -or
            $publishedKeyIds -notcontains $previousKeyId) {
        throw 'ms-auth did not publish both the active and retained public signing keys.'
    }

    $authBaseUri = "$issuer/api/v1/auth"
    $authOrigin = 'http://localhost:5173'
    $clientWebSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $csrf = Invoke-RestMethod -Uri "$authBaseUri/csrf" -WebSession $clientWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $clientWebSession

    $email = "e2e-$runId@gmail.com"
    $password = 'TestE2E26!Secure'
    $registrationRequest = @{
        rut = New-TestRut
        nombre = 'JWT E2E'
        apellidos = 'Test'
        correo = $email
        password = $password
        region = 'metropolitana'
        comuna = 'Santiago Centro'
        direccion = 'Direccion temporal de prueba'
    }
    $registration = $registrationRequest | ConvertTo-Json -Compress
    $missingCsrfHeaders = @{ Origin = $authOrigin }
    Assert-Status 'auth register without CSRF' 403 (Get-AuthHttpStatus -Uri "$authBaseUri/register" `
        -Method Post -Session $clientWebSession -Headers $missingCsrfHeaders -Body $registration)
    $wrongOriginHeaders = @{ Origin = 'https://attacker.example'; 'X-CSRF-TOKEN' = $csrf.token }
    Assert-Status 'auth register with untrusted Origin' 403 (Get-AuthHttpStatus -Uri "$authBaseUri/register" `
        -Method Post -Session $clientWebSession -Headers $wrongOriginHeaders -Body $registration)

    $registerHeaders = @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $csrf.token }
    $shortPasswordRequest = $registrationRequest.Clone()
    $shortPasswordRequest.password = 'short'
    Assert-Status 'auth register with short password' 400 (Get-AuthHttpStatus -Uri "$authBaseUri/register" `
        -Method Post -Session $clientWebSession -Headers $registerHeaders `
        -Body ($shortPasswordRequest | ConvertTo-Json -Compress))
    $registerResponse = Invoke-WebRequest -Uri "$authBaseUri/register" `
        -Method Post -WebSession $clientWebSession -Headers $registerHeaders `
        -ContentType 'application/json' -Body $registration -UseBasicParsing -TimeoutSec 20
    Assert-Status 'ms-auth register + CSRF' 201 ([int]$registerResponse.StatusCode)
    $registered = $registerResponse.Content | ConvertFrom-Json
    $script:userId = $registered.user.id
    if ($registered.user.id -ne $script:userId -or [string]::IsNullOrWhiteSpace($registered.accessToken)) {
        throw 'Registration did not return the public user profile and access token.'
    }
    $registerCookies = @($registerResponse.Headers['Set-Cookie']) -join ';'
    if ($registered.PSObject.Properties.Name -contains 'refreshToken' -or
            $registerCookies -notmatch 'gz_refresh=.*Path=/api/v1/auth.*HttpOnly.*SameSite=Lax' -or
            $registerCookies -match 'Domain=') {
        throw 'Registration exposed the refresh token or set an unsafe cookie.'
    }
    if ([string]::IsNullOrWhiteSpace($script:userId)) {
        throw 'The registration response did not contain a user ID.'
    }
    $clientToken = $registered.accessToken
    $clientClaims = Get-JwtClaims -Token $clientToken
    $expectedAudiences = @('ms-catalogo', 'ms-carrito', 'ms-contacto', 'ms-pedido')
    if ($registered.user.id -ne $script:userId -or $registered.user.rol -ne 'CLIENTE' -or
            @($clientClaims.roles) -notcontains 'CLIENTE') {
        throw 'The registration access JWT has an unexpected subject or role.'
    }
    foreach ($audience in $expectedAudiences) {
        if (@($clientClaims.aud) -notcontains $audience) {
            throw "The client JWT is missing audience $audience."
        }
    }
    $clientProfile = Invoke-RestMethod -Uri "$authBaseUri/me" `
        -Headers @{ Authorization = "Bearer $clientToken" } -TimeoutSec 20
    if ($clientProfile.id -ne $script:userId -or $clientProfile.rol -ne 'CLIENTE' -or
            $clientProfile.PSObject.Properties.Name -contains 'passwordHash' -or
            $clientProfile.PSObject.Properties.Name -contains 'rut') {
        throw 'The authenticated profile contains unexpected or sensitive fields.'
    }
    $script:results += 'ms-auth GET /me: HTTP 200; minimal profile verified'
    $previousKeyToken = New-TestJwt -Subject $script:userId `
        -SigningPrivateKey $previousKeyPair.privateKey -SigningKeyId $previousKeyId
    Assert-Status 'ms-auth JWT signed by retained RS256 key' 200 `
        (Get-HttpStatus "$authBaseUri/me" $previousKeyToken)
    $script:results += 'ms-auth previous RS256 key: JWKS published and JWT accepted during rotation overlap'
    Assert-Status 'ms-auth GET /me without bearer' 401 (Get-HttpStatus -Uri "$authBaseUri/me")
    $script:results += 'ms-auth register: HTTP 201; CLIENTE JWT, audiences, refresh cookie and JWKS verified'

    $csrf = Invoke-RestMethod -Uri "$authBaseUri/csrf" -WebSession $clientWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $clientWebSession
    $oldRefreshToken = Get-AuthCookieValue -Session $clientWebSession -Name 'gz_refresh'
    if ([string]::IsNullOrWhiteSpace($oldRefreshToken)) {
        throw 'Registration did not set the HttpOnly refresh cookie.'
    }
    $refreshHeaders = @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $csrf.token }
    $rotatedLogin = Invoke-RestMethod -Uri "$authBaseUri/refresh" -Method Post `
        -WebSession $clientWebSession -Headers $refreshHeaders -TimeoutSec 20
    $newRefreshToken = Get-AuthCookieValue -Session $clientWebSession -Name 'gz_refresh'
    if ([string]::IsNullOrWhiteSpace($newRefreshToken) -or $newRefreshToken -eq $oldRefreshToken) {
        throw 'Refresh did not rotate the HttpOnly refresh token.'
    }
    $clientToken = $rotatedLogin.accessToken
    $clientClaims = Get-JwtClaims -Token $clientToken
    if (@($clientClaims.roles) -notcontains 'CLIENTE') {
        throw 'Refresh returned an access token without the CLIENTE role.'
    }
    $script:results += 'ms-auth POST /refresh: HTTP 200; refresh token rotated'

    $csrfAfterRefresh = Invoke-RestMethod -Uri "$authBaseUri/csrf" `
        -WebSession $clientWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $clientWebSession
    $staleCsrfHeaders = @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $csrf.token }
    Assert-Status 'auth refresh with stale CSRF token' 403 (Get-AuthHttpStatus -Uri "$authBaseUri/refresh" `
        -Method Post -Session $clientWebSession -Headers $staleCsrfHeaders)

    $replayWebSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $contextCookie = Get-AuthCookieValue -Session $clientWebSession -Name 'gz_csrf_context'
    $replayContextCookie = New-Object -TypeName System.Net.Cookie `
        -ArgumentList @('gz_csrf_context', $contextCookie, '/api/v1', 'localhost')
    $replayRefreshCookie = New-Object -TypeName System.Net.Cookie `
        -ArgumentList @('gz_refresh', $oldRefreshToken, '/api/v1/auth', 'localhost')
    $replayWebSession.Cookies.Add($replayContextCookie)
    $replayWebSession.Cookies.Add($replayRefreshCookie)
    $replayedTokenHeaders = @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $csrfAfterRefresh.token }
    Assert-Status 'auth refresh token reuse' 401 (Get-AuthHttpStatus -Uri "$authBaseUri/refresh" `
        -Method Post -Session $replayWebSession -Headers $replayedTokenHeaders)
    Assert-Status 'auth current refresh after family reuse' 401 (Get-AuthHttpStatus -Uri "$authBaseUri/refresh" `
        -Method Post -Session $clientWebSession -Headers $replayedTokenHeaders)
    $script:results += 'ms-auth refresh token reuse: HTTP 401; token family revoked'

    $loginBody = @{ correo = $email; password = $password } | ConvertTo-Json -Compress
    $csrf = Invoke-RestMethod -Uri "$authBaseUri/csrf" -WebSession $clientWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $clientWebSession
    $clientLogin = Invoke-RestMethod -Uri "$authBaseUri/login" `
        -Method Post -WebSession $clientWebSession `
        -Headers @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $csrf.token } `
        -ContentType 'application/json' -Body $loginBody -TimeoutSec 20
    $clientToken = $clientLogin.accessToken
    if ($clientLogin.user.id -ne $script:userId -or
            $clientLogin.PSObject.Properties.Name -contains 'refreshToken') {
        throw 'Login returned an unexpected user or exposed the refresh token.'
    }
    $script:results += 'ms-auth POST /login: HTTP 200; refresh kept out of JSON'

    $csrfForLogout = Invoke-RestMethod -Uri "$authBaseUri/csrf" -WebSession $clientWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $clientWebSession
    $logoutResponse = Invoke-WebRequest -Uri "$authBaseUri/logout" -Method Post `
        -WebSession $clientWebSession `
        -Headers @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $csrfForLogout.token } `
        -UseBasicParsing -TimeoutSec 20
    Assert-Status 'ms-auth POST /logout' 204 ([int]$logoutResponse.StatusCode)
    if ($logoutResponse.Headers['Set-Cookie'] -notmatch 'gz_refresh=.*Max-Age=0' -or
            $logoutResponse.Headers['Set-Cookie'] -notmatch 'gz_csrf_context=.*Max-Age=0') {
        throw 'Logout did not clear the refresh and CSRF context cookies.'
    }
    $csrf = Invoke-RestMethod -Uri "$authBaseUri/csrf" -WebSession $clientWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $clientWebSession
    Assert-Status 'auth refresh after logout' 401 (Get-AuthHttpStatus -Uri "$authBaseUri/refresh" `
        -Method Post -Session $clientWebSession `
        -Headers @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $csrf.token })

    $adminWebSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $adminCsrf = Invoke-RestMethod -Uri "$authBaseUri/csrf" -WebSession $adminWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $adminWebSession
    $adminLoginBody = @{ correo = $bootstrapAdminEmail; password = $bootstrapAdminPassword } |
        ConvertTo-Json -Compress
    $adminLogin = Invoke-RestMethod -Uri "$authBaseUri/login" `
        -Method Post -WebSession $adminWebSession `
        -Headers @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $adminCsrf.token } `
        -ContentType 'application/json' -Body $adminLoginBody -TimeoutSec 20
    $script:bootstrapAdminId = $adminLogin.user.id
    $adminToken = $adminLogin.accessToken
    $adminClaims = Get-JwtClaims -Token $adminToken
    if ([string]::IsNullOrWhiteSpace($script:bootstrapAdminId) -or
            @($adminClaims.roles) -notcontains 'ADMINISTRADOR' -or
            $adminLogin.user.rol -ne 'ADMINISTRADOR') {
        throw 'ms-auth bootstrap did not create an authenticatable ADMINISTRADOR account.'
    }
    $adminUsersUri = "$issuer/api/v1/admin/users"
    Assert-Status 'admin users without token' 401 (Get-HttpStatus $adminUsersUri)
    Assert-Status 'admin users with CLIENTE token' 403 `
        (Get-HttpStatus $adminUsersUri $clientToken)
    $adminListUri = $adminUsersUri + '?page=0&size=20'
    $adminPage = Invoke-RestMethod -Uri $adminListUri `
        -Headers @{ Authorization = "Bearer $adminToken" } -TimeoutSec 20
    $listedClient = @($adminPage.content | Where-Object { $_.id -eq $script:userId })[0]
    $listedAdmin = @($adminPage.content | Where-Object { $_.id -eq $script:bootstrapAdminId })[0]
    if ($adminPage.totalElements -lt 2 -or $listedClient.rol -ne 'CLIENTE' -or
            $listedAdmin.rol -ne 'ADMINISTRADOR' -or $adminPage.content[0].PSObject.Properties.Name -contains 'passwordHash') {
        throw 'Administrative user listing did not return safe, filtered account profiles.'
    }
    $clientFilterUri = $adminUsersUri + '?role=CLIENTE&status=ACTIVE&page=0&size=20'
    $filteredUsers = Invoke-RestMethod -Uri $clientFilterUri `
        -Headers @{ Authorization = "Bearer $adminToken" } -TimeoutSec 20
    if ($filteredUsers.content.Count -lt 1 -or
            @($filteredUsers.content | Where-Object { $_.rol -ne 'CLIENTE' -or $_.status -ne 'ACTIVE' }).Count -gt 0) {
        throw 'Administrative user filters returned an account outside the requested role and status.'
    }
    $adminClientDetail = Invoke-RestMethod -Uri "$adminUsersUri/$($script:userId)" `
        -Headers @{ Authorization = "Bearer $adminToken" } -TimeoutSec 20
    if ($adminClientDetail.rut -ne $registrationRequest.rut.Replace('.', '').Replace('-', '') -or
            $adminClientDetail.PSObject.Properties.Name -contains 'passwordHash') {
        throw 'Administrative user detail is missing expected profile fields or exposed credentials.'
    }
    $updatedProfile = Invoke-RestMethod -Uri "$adminUsersUri/$($script:userId)" -Method Patch `
        -Headers @{ Authorization = "Bearer $adminToken" } -ContentType 'application/json' `
        -Body (@{ apellidos = 'E2E Updated' } | ConvertTo-Json -Compress) -TimeoutSec 20
    if ($updatedProfile.apellidos -ne 'E2E Updated') {
        throw 'Administrative profile update did not persist the editable field.'
    }
    $promotedUser = Invoke-RestMethod -Uri "$adminUsersUri/$($script:userId)/role" -Method Patch `
        -Headers @{ Authorization = "Bearer $adminToken" } -ContentType 'application/json' `
        -Body (@{ rol = 'ADMINISTRADOR' } | ConvertTo-Json -Compress) -TimeoutSec 20
    if ($promotedUser.rol -ne 'ADMINISTRADOR') {
        throw 'Administrative role change did not promote the client.'
    }
    $demotedUser = Invoke-RestMethod -Uri "$adminUsersUri/$($script:userId)/role" -Method Patch `
        -Headers @{ Authorization = "Bearer $adminToken" } -ContentType 'application/json' `
        -Body (@{ rol = 'CLIENTE' } | ConvertTo-Json -Compress) -TimeoutSec 20
    if ($demotedUser.rol -ne 'CLIENTE') {
        throw 'Administrative role change did not restore the client role.'
    }
    $script:results += 'ms-auth admin accounts: bootstrap, secure listing/detail, profile and role changes verified'
    Assert-Status 'protect last administrator status' 409 (Get-HttpStatus `
        -Uri "$adminUsersUri/$($script:bootstrapAdminId)/status" -Token $adminToken -Method Patch `
        -Body (@{ status = 'INACTIVE' } | ConvertTo-Json -Compress))
    Assert-Status 'protect last administrator role' 409 (Get-HttpStatus `
        -Uri "$adminUsersUri/$($script:bootstrapAdminId)/role" -Token $adminToken -Method Patch `
        -Body (@{ rol = 'CLIENTE' } | ConvertTo-Json -Compress))
    Assert-Status 'protect last administrator delete' 409 `
        (Get-HttpStatus "$adminUsersUri/$($script:bootstrapAdminId)" $adminToken -Method Delete)
    $script:results += 'ms-auth admin safeguard: last active administrator cannot be disabled or demoted'
    $wrongAudienceToken = New-TestJwt -Subject $script:userId -Audiences @('ms-not-a-resource-server')
    $wrongIssuerToken = New-TestJwt -Subject $script:userId -TokenIssuer "$issuer/untrusted"
    $expiredToken = New-TestJwt -Subject $script:userId -ExpirationOffsetSeconds -600
    $missingRolesToken = New-TestJwt -Subject $script:userId -OmitRoles
    $unrecognizedRoleToken = New-TestJwt -Subject $script:userId -Roles @('AUDITOR')
    $malformedRolesToken = New-TestJwt -Subject $script:userId -Roles @{ unexpected = 'ADMINISTRADOR' }

    $null = Start-TestService -Name 'ms-catalogo' -Schema 'GZ_TEST_CATALOGO' -Port 8181 -Audience 'ms-catalogo'
    Assert-Status 'catalog public + CLIENTE JWT' 200 (Get-HttpStatus 'http://localhost:8181/api/v1/products?page=0&size=1' $clientToken)
    Assert-Status 'catalog public + retained-key JWT' 200 `
        (Get-HttpStatus 'http://localhost:8181/api/v1/products?page=0&size=1' $previousKeyToken)
    Assert-Status 'catalog admin route + CLIENTE' 403 (Get-HttpStatus 'http://localhost:8181/api/v1/admin/products?page=0&size=1' $clientToken)
    $catalogAdminUri = 'http://localhost:8181/api/v1/admin/products?page=0&size=1'
    Assert-Status 'catalog admin + ADMINISTRADOR JWT' 200 (Get-HttpStatus $catalogAdminUri $adminToken)

    $catalogBaseUri = 'http://localhost:8181/api/v1/admin/products'
    $catalogHeaders = @{ Authorization = "Bearer $adminToken" }
    $catalogCreateBody = @{
        titulo = "E2E Product $runId"
        precio = 1000
        categoria = $catalogCategory
        imagen = 'src/img/e2e-cover.png'
        descripcionCorta = 'E2E admin API test'
        descripcion = 'Temporary product for the Oracle admin API test.'
        video = $null
        galeria = @('src/img/e2e-gallery.png')
        detalles = @{
            desarrollador = $catalogDeveloper
            editor = $catalogPublisher
            fechaLanzamiento = '2026-01-01'
            modos = @($catalogMode)
            idiomas = @($catalogLanguage)
        }
        requisitos = @{
            minimos = @{
                so = 'Windows 10'
                procesador = 'E2E CPU'
                memoria = '8 GB'
                graficos = 'E2E GPU'
                directx = 'DirectX 12'
                almacenamiento = '20 GB'
            }
            recomendados = @{
                so = 'Windows 11'
                procesador = 'E2E CPU recommended'
                memoria = '16 GB'
                graficos = 'E2E GPU recommended'
                directx = 'DirectX 12'
                almacenamiento = '20 GB'
            }
        }
    }
    $catalogCreateResponse = Invoke-WebRequest -Uri $catalogBaseUri `
        -Method Post -Headers $catalogHeaders -ContentType 'application/json' `
        -Body ($catalogCreateBody | ConvertTo-Json -Depth 8 -Compress) -UseBasicParsing -TimeoutSec 20
    Assert-Status 'catalog admin create' 201 ([int]$catalogCreateResponse.StatusCode)
    $createdProduct = $catalogCreateResponse.Content | ConvertFrom-Json
    $script:catalogProductId = [string]$createdProduct.id
    if ($script:catalogProductId -notmatch '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$') {
        throw 'Catalog create response did not contain a valid product UUID.'
    }

    $catalogDetailUri = "$catalogBaseUri/$($script:catalogProductId)"
    $catalogDetail = Invoke-RestMethod -Uri $catalogDetailUri -Headers $catalogHeaders -TimeoutSec 20
    if ($catalogDetail.status -ne 'ACTIVE' -or $catalogDetail.titulo -ne $catalogCreateBody.titulo) {
        throw 'Catalog admin detail did not return the newly created active product.'
    }
    $script:results += 'catalog admin detail: HTTP 200; new product verified'

    $catalogUpdateBody = @{
        titulo = "E2E Updated Product $runId"
        precio = 2000
        categoria = $catalogUpdatedCategory
        imagen = 'src/img/e2e-cover-updated.png'
        descripcionCorta = 'Updated E2E admin API test'
        descripcion = 'Updated temporary product for the Oracle admin API test.'
        video = $null
        galeria = @('src/img/e2e-gallery-updated.png')
        detalles = @{
            desarrollador = "$catalogDeveloper Updated"
            editor = "$catalogPublisher Updated"
            fechaLanzamiento = '2026-02-02'
            modos = @("$catalogMode Updated")
            idiomas = @("$catalogLanguage Updated")
        }
        requisitos = $catalogCreateBody.requisitos
    }
    $catalogUpdated = Invoke-RestMethod -Uri $catalogDetailUri -Method Put `
        -Headers $catalogHeaders -ContentType 'application/json' `
        -Body ($catalogUpdateBody | ConvertTo-Json -Depth 8 -Compress) -TimeoutSec 20
    if ($catalogUpdated.status -ne 'ACTIVE' -or
            $catalogUpdated.titulo -ne $catalogUpdateBody.titulo -or
            $catalogUpdated.galeria.Count -ne 1 -or
            $catalogUpdated.galeria[0] -ne 'src/img/e2e-gallery-updated.png') {
        throw 'Catalog PUT did not replace the product representation or preserve its status.'
    }
    $script:results += 'catalog admin update: HTTP 200; relations replaced and ACTIVE status preserved'

    $archivedStatusBody = @{ status = 'ARCHIVED' } | ConvertTo-Json -Compress
    $catalogArchived = Invoke-RestMethod -Uri "$catalogDetailUri/status" -Method Patch `
        -Headers $catalogHeaders -ContentType 'application/json' -Body $archivedStatusBody -TimeoutSec 20
    if ($catalogArchived.status -ne 'ARCHIVED') {
        throw 'Catalog PATCH did not archive the product.'
    }
    $script:results += 'catalog admin PATCH archive: HTTP 200'

    $activeStatusBody = @{ status = 'ACTIVE' } | ConvertTo-Json -Compress
    $catalogReactivated = Invoke-RestMethod -Uri "$catalogDetailUri/status" -Method Patch `
        -Headers $catalogHeaders -ContentType 'application/json' -Body $activeStatusBody -TimeoutSec 20
    if ($catalogReactivated.status -ne 'ACTIVE') {
        throw 'Catalog PATCH did not reactivate the product.'
    }
    $script:results += 'catalog admin PATCH reactivate: HTTP 200'

    $archiveResponse = Invoke-WebRequest -Uri $catalogDetailUri -Method Delete `
        -Headers $catalogHeaders -UseBasicParsing -TimeoutSec 20
    Assert-Status 'catalog admin DELETE archive' 204 ([int]$archiveResponse.StatusCode)
    $archivedDetail = Invoke-RestMethod -Uri $catalogDetailUri -Headers $catalogHeaders -TimeoutSec 20
    if ($archivedDetail.status -ne 'ARCHIVED') {
        throw 'Catalog DELETE did not preserve the product as ARCHIVED.'
    }
    Assert-Status 'catalog public detail for archived product' 404 `
        (Get-HttpStatus "http://localhost:8181/api/v1/products/$($script:catalogProductId)" $clientToken)

    Assert-Status 'catalog wrong audience' 401 (Get-HttpStatus 'http://localhost:8181/api/v1/products?page=0&size=1' $wrongAudienceToken)
    Assert-Status 'catalog missing token' 401 (Get-HttpStatus -Uri $catalogAdminUri)
    Assert-Status 'catalog wrong issuer' 401 (Get-HttpStatus $catalogAdminUri $wrongIssuerToken)
    Assert-Status 'catalog expired token' 401 (Get-HttpStatus $catalogAdminUri $expiredToken)
    Assert-Status 'catalog missing roles' 403 (Get-HttpStatus $catalogAdminUri $missingRolesToken)
    Assert-Status 'catalog unrecognized role' 403 (Get-HttpStatus $catalogAdminUri $unrecognizedRoleToken)
    Assert-Status 'catalog malformed roles' 403 (Get-HttpStatus $catalogAdminUri $malformedRolesToken)

    $null = Start-TestService -Name 'ms-carrito' -Schema 'GZ_TEST_CARRITO' -Port 8183 -Audience 'ms-carrito'
    Assert-Status 'cart + CLIENTE JWT' 200 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $clientToken)
    Assert-Status 'cart + ADMINISTRADOR role' 403 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $adminToken)
    Assert-Status 'cart malformed JWT' 401 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' 'not-a-valid-jwt')
    Assert-Status 'cart wrong audience' 401 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $wrongAudienceToken)
    Assert-Status 'cart missing token' 401 (Get-HttpStatus -Uri 'http://localhost:8183/api/v1/cart')
    Assert-Status 'cart wrong issuer' 401 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $wrongIssuerToken)
    Assert-Status 'cart expired token' 401 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $expiredToken)
    Assert-Status 'cart missing roles' 403 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $missingRolesToken)
    Assert-Status 'cart unrecognized role' 403 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $unrecognizedRoleToken)
    Assert-Status 'cart malformed roles' 403 (Get-HttpStatus 'http://localhost:8183/api/v1/cart' $malformedRolesToken)

    $null = Start-TestService -Name 'ms-contacto' -Schema 'GZ_TEST_CONTACTO' -Port 8185 -Audience 'ms-contacto'
    $contactBody = @{
        nombre = 'JWT E2E'
        correo = $email
        asunto = 'Prueba JWT'
        mensaje = 'Mensaje de prueba automatizada'
    } | ConvertTo-Json -Compress
    $contact = Invoke-RestMethod -Uri 'http://localhost:8185/api/v1/contact-messages' `
        -Method Post -Headers @{ Authorization = "Bearer $clientToken" } `
        -ContentType 'application/json' -Body $contactBody -TimeoutSec 20
    if ($contact.userId -ne $script:userId) {
        throw 'ms-contacto did not associate the message with the JWT subject.'
    }
    $script:results += 'contact POST + CLIENTE JWT: HTTP 201; subject association verified'
    $contactAdminUri = 'http://localhost:8185/api/v1/admin/contact-messages?page=0&size=1'
    Assert-Status 'contact admin route + CLIENTE' 403 (Get-HttpStatus $contactAdminUri $clientToken)
    Assert-Status 'contact admin route + ADMINISTRADOR' 200 (Get-HttpStatus $contactAdminUri $adminToken)
    Assert-Status 'contact wrong audience' 401 (Get-HttpStatus $contactAdminUri $wrongAudienceToken)
    Assert-Status 'contact missing token' 401 (Get-HttpStatus -Uri $contactAdminUri)
    Assert-Status 'contact wrong issuer' 401 (Get-HttpStatus $contactAdminUri $wrongIssuerToken)
    Assert-Status 'contact expired token' 401 (Get-HttpStatus $contactAdminUri $expiredToken)
    Assert-Status 'contact missing roles' 403 (Get-HttpStatus $contactAdminUri $missingRolesToken)
    Assert-Status 'contact unrecognized role' 403 (Get-HttpStatus $contactAdminUri $unrecognizedRoleToken)
    Assert-Status 'contact malformed roles' 403 (Get-HttpStatus $contactAdminUri $malformedRolesToken)

    $null = Start-TestService -Name 'ms-pedido' -Schema 'GZ_TEST_PEDIDO' -Port 8186 -Audience 'ms-pedido'
    Assert-Status 'orders/me + CLIENTE JWT' 200 (Get-HttpStatus 'http://localhost:8186/api/v1/orders/me?page=0&size=1' $clientToken)
    $ordersAdminUri = 'http://localhost:8186/api/v1/admin/orders?page=0&size=1'
    Assert-Status 'admin/orders + CLIENTE' 403 (Get-HttpStatus $ordersAdminUri $clientToken)
    Assert-Status 'admin/orders + ADMINISTRADOR JWT' 200 (Get-HttpStatus $ordersAdminUri $adminToken)
    Assert-Status 'orders wrong audience' 401 (Get-HttpStatus 'http://localhost:8186/api/v1/orders/me?page=0&size=1' $wrongAudienceToken)
    Assert-Status 'orders missing token' 401 (Get-HttpStatus -Uri $ordersAdminUri)
    Assert-Status 'orders wrong issuer' 401 (Get-HttpStatus $ordersAdminUri $wrongIssuerToken)
    Assert-Status 'orders expired token' 401 (Get-HttpStatus $ordersAdminUri $expiredToken)
    Assert-Status 'orders missing roles' 403 (Get-HttpStatus $ordersAdminUri $missingRolesToken)
    Assert-Status 'orders unrecognized role' 403 (Get-HttpStatus $ordersAdminUri $unrecognizedRoleToken)
    Assert-Status 'orders malformed roles' 403 (Get-HttpStatus $ordersAdminUri $malformedRolesToken)

    $inactiveWebSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $inactiveCsrf = Invoke-RestMethod -Uri "$authBaseUri/csrf" `
        -WebSession $inactiveWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $inactiveWebSession
    $inactiveLogin = Invoke-RestMethod -Uri "$authBaseUri/login" `
        -Method Post -WebSession $inactiveWebSession `
        -Headers @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $inactiveCsrf.token } `
        -ContentType 'application/json' -Body $loginBody -TimeoutSec 20
    $inactiveAccessToken = $inactiveLogin.accessToken
    $inactiveCsrf = Invoke-RestMethod -Uri "$authBaseUri/csrf" `
        -WebSession $inactiveWebSession -TimeoutSec 20
    Add-CsrfContextForCleanup -Session $inactiveWebSession
    $inactiveResponse = Invoke-RestMethod -Uri "$adminUsersUri/$($script:userId)/status" -Method Patch `
        -Headers @{ Authorization = "Bearer $adminToken" } -ContentType 'application/json' `
        -Body (@{ status = 'INACTIVE' } | ConvertTo-Json -Compress) -TimeoutSec 20
    if ($inactiveResponse.status -ne 'INACTIVE') {
        throw 'Administrative status route did not deactivate the account.'
    }
    Assert-Status 'auth refresh for inactive account' 401 (Get-AuthHttpStatus -Uri "$authBaseUri/refresh" `
        -Method Post -Session $inactiveWebSession `
        -Headers @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $inactiveCsrf.token })
    Assert-Status 'auth GET /me for inactive account' 401 `
        (Get-HttpStatus "$authBaseUri/me" $inactiveAccessToken)
    $reactivated = Invoke-RestMethod -Uri "$adminUsersUri/$($script:userId)/status" -Method Patch `
        -Headers @{ Authorization = "Bearer $adminToken" } -ContentType 'application/json' `
        -Body (@{ status = 'ACTIVE' } | ConvertTo-Json -Compress) -TimeoutSec 20
    if ($reactivated.status -ne 'ACTIVE') {
        throw 'Administrative status route did not reactivate the account.'
    }
    Assert-Status 'auth refresh after reactivation with revoked family' 401 `
        (Get-AuthHttpStatus -Uri "$authBaseUri/refresh" -Method Post -Session $inactiveWebSession `
            -Headers @{ Origin = $authOrigin; 'X-CSRF-TOKEN' = $inactiveCsrf.token })
    $deleteAccountResponse = Invoke-WebRequest -Uri "$adminUsersUri/$($script:userId)" -Method Delete `
        -Headers @{ Authorization = "Bearer $adminToken" } -UseBasicParsing -TimeoutSec 20
    Assert-Status 'admin DELETE user and anonymize' 204 ([int]$deleteAccountResponse.StatusCode)
    $anonymized = Invoke-RestMethod -Uri "$adminUsersUri/$($script:userId)" `
        -Headers @{ Authorization = "Bearer $adminToken" } -TimeoutSec 20
    if ($anonymized.status -ne 'INACTIVE' -or [string]::IsNullOrWhiteSpace($anonymized.anonymizedAt) -or
            $anonymized.rut -ne $null -or $anonymized.nombre -ne $null -or
            $anonymized.correo -notmatch '^deleted\+') {
        throw 'Account deletion did not anonymize the user profile.'
    }
    $contactCountSql = @"
SET PAGESIZE 0 FEEDBACK OFF HEADING OFF VERIFY OFF
ALTER SESSION SET CONTAINER = XEPDB1;
SELECT COUNT(*) FROM GZ_TEST_CONTACTO.contact_messages WHERE user_id = '$($script:userId)';
EXIT
"@
    $remainingContactMessages = ((Invoke-OracleSql -Sql $contactCountSql) -join '').Trim()
    if ($remainingContactMessages -ne '0') {
        throw 'Account deletion left contact messages associated with the anonymized user.'
    }
    $script:results += 'ms-auth admin deletion: sessions revoked, contact messages removed and profile anonymized'
} catch {
    $script:failure = "$($_.Exception.Message) (line $($_.InvocationInfo.ScriptLineNumber): $($_.InvocationInfo.Line.Trim()))"
} finally {
    foreach ($service in $script:services) {
        Stop-TestService -Service $service
    }

    if ($script:catalogProductId -match '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$') {
        $catalogCleanupSql = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE
ALTER SESSION SET CONTAINER = XEPDB1;
DELETE FROM GZ_TEST_CATALOGO.products WHERE product_id = '$($script:catalogProductId)';
DELETE FROM GZ_TEST_CATALOGO.categories
    WHERE name IN ('$catalogCategory', '$catalogUpdatedCategory')
      AND NOT EXISTS (
          SELECT 1 FROM GZ_TEST_CATALOGO.products p
          WHERE p.category_id = GZ_TEST_CATALOGO.categories.category_id
      );
DELETE FROM GZ_TEST_CATALOGO.companies
    WHERE name IN ('$catalogDeveloper', '$catalogPublisher', '$catalogDeveloper Updated', '$catalogPublisher Updated')
      AND NOT EXISTS (
          SELECT 1 FROM GZ_TEST_CATALOGO.product_companies pc
          WHERE pc.company_id = GZ_TEST_CATALOGO.companies.company_id
      );
DELETE FROM GZ_TEST_CATALOGO.game_modes
    WHERE name IN ('$catalogMode', '$catalogMode Updated')
      AND NOT EXISTS (
          SELECT 1 FROM GZ_TEST_CATALOGO.product_game_modes pgm
          WHERE pgm.mode_id = GZ_TEST_CATALOGO.game_modes.mode_id
      );
DELETE FROM GZ_TEST_CATALOGO.languages
    WHERE name IN ('$catalogLanguage', '$catalogLanguage Updated')
      AND NOT EXISTS (
          SELECT 1 FROM GZ_TEST_CATALOGO.product_languages pl
          WHERE pl.language_id = GZ_TEST_CATALOGO.languages.language_id
      );
COMMIT;
EXIT
"@
        try {
            Invoke-OracleSql -Sql $catalogCleanupSql | Out-Null
        } catch {
            $script:failure = 'E2E cleanup of temporary catalog rows failed.'
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($bootstrapAdminEmail) -or
            $script:userId -match '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' -or
            $script:csrfContextHashes.Count -gt 0) {
        $cleanupStatements = @(
            'WHENEVER SQLERROR EXIT SQL.SQLCODE',
            'ALTER SESSION SET CONTAINER = XEPDB1;'
        )
        $accountIdList = @($script:userId, $script:bootstrapAdminId) |
            Where-Object { $_ -match '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' } |
            ForEach-Object { "'$_'" }
        $accountIdsSql = if ($accountIdList.Count -gt 0) { $accountIdList -join ',' } else { "''" }
        if ($accountIdList.Count -gt 0 -or -not [string]::IsNullOrWhiteSpace($bootstrapAdminEmail)) {
            $testAccounts = "(email_normalized = '$bootstrapAdminEmail' OR id IN ($accountIdsSql))"
            $cleanupStatements += "DELETE FROM GZ_TEST_CONTACTO.contact_messages WHERE user_id IN (SELECT id FROM GZ_TEST_AUTH.users WHERE $testAccounts);"
            $cleanupStatements += "DELETE FROM GZ_TEST_AUTH.refresh_tokens WHERE family_id IN (SELECT family_id FROM GZ_TEST_AUTH.refresh_token_families WHERE user_id IN (SELECT id FROM GZ_TEST_AUTH.users WHERE $testAccounts));"
            $cleanupStatements += "DELETE FROM GZ_TEST_AUTH.refresh_token_families WHERE user_id IN (SELECT id FROM GZ_TEST_AUTH.users WHERE $testAccounts);"
        }
        if ($script:csrfContextHashes.Count -gt 0) {
            $contextHashList = ($script:csrfContextHashes | ForEach-Object { "'$_'" }) -join ','
            $cleanupStatements += "DELETE FROM GZ_TEST_AUTH.csrf_tokens WHERE context_id IN (SELECT context_id FROM GZ_TEST_AUTH.csrf_contexts WHERE context_hash IN ($contextHashList));"
            $cleanupStatements += "DELETE FROM GZ_TEST_AUTH.csrf_contexts WHERE context_hash IN ($contextHashList);"
        }
        if ($accountIdList.Count -gt 0 -or -not [string]::IsNullOrWhiteSpace($bootstrapAdminEmail)) {
            $cleanupStatements += "DELETE FROM GZ_TEST_AUTH.users WHERE email_normalized = '$bootstrapAdminEmail' OR id IN ($accountIdsSql);"
        }
        $cleanupStatements += @('COMMIT;', 'EXIT')
        $cleanupSql = $cleanupStatements -join "`n"
        try {
            Invoke-OracleSql -Sql $cleanupSql | Out-Null
        } catch {
            $script:failure = 'E2E cleanup of temporary auth and contact rows failed.'
        }
    }

    if ($script:failure) {
        foreach ($logFile in $script:logFiles) {
            if (Test-Path $logFile) {
                Get-Content $logFile | Select-String 'ERROR|Exception|Caused by:|Started .*Application' |
                    Select-Object -Last 8 | ForEach-Object { Write-Host $_.Line }
            }
        }
    }
    if ($script:failure) {
        Write-Host "E2E logs preserved for diagnosis: $logDirectory"
    } else {
        Remove-Item -LiteralPath $logDirectory -Recurse -Force -ErrorAction SilentlyContinue
    }
}

if ($script:failure) {
    throw "JWT integration test failed: $($script:failure)"
}
$script:results
