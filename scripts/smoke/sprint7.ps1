Write-Host "[SPR-007] Smoke IAM Gate"
Write-Host ""

$adminEmail = $env:SISFERRETE_ADMIN_EMAIL
$adminPassword = $env:SISFERRETE_ADMIN_PASSWORD
$cajeroEmail = $env:SISFERRETE_CAJERO_EMAIL
$cajeroPassword = $env:SISFERRETE_CAJERO_PASSWORD

if ([string]::IsNullOrWhiteSpace($adminEmail) -or [string]::IsNullOrWhiteSpace($adminPassword)) {
  Write-Host "Define SISFERRETE_ADMIN_EMAIL y SISFERRETE_ADMIN_PASSWORD."
  exit 1
}

if ([string]::IsNullOrWhiteSpace($cajeroEmail) -or [string]::IsNullOrWhiteSpace($cajeroPassword)) {
  Write-Host "Define SISFERRETE_CAJERO_EMAIL y SISFERRETE_CAJERO_PASSWORD."
  exit 1
}

$adminBody = @{
  email    = $adminEmail
  password = $adminPassword
} | ConvertTo-Json

Write-Host "1) Login admin..."
$adminLogin = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $adminBody
$adminToken = $adminLogin.accessToken
$headers = @{ Authorization = "Bearer $adminToken" }

Write-Host "2) Obtener sucursales..."
$branches = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/admin/branches" -Headers $headers
$branch = $branches | Where-Object { $_.code -eq "MATRIZ" } | Select-Object -First 1
if (-not $branch -and $branches.Count -gt 0) {
  $branch = $branches[0]
}
if (-not $branch) {
  Write-Host "No hay sucursales disponibles."
  exit 1
}
Write-Host ("Sucursal usada: {0} ({1})" -f $branch.name, $branch.branchId)

Write-Host "3) Buscar/crear usuario cajero..."
$users = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/admin/users" -Headers $headers
$user = $users | Where-Object { $_.email -eq $cajeroEmail } | Select-Object -First 1
if (-not $user) {
  $createBody = @{
    email    = $cajeroEmail
    fullName = "Cajero Demo"
    password = $cajeroPassword
    isActive = $true
  } | ConvertTo-Json
  $user = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/users" -Headers $headers -ContentType "application/json" -Body $createBody
}
Write-Host ("UserId: {0}" -f $user.userId)

Write-Host "4) Asignar rol CAJERO..."
$rolesBody = @{ roleCodes = @("CAJERO") } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri ("http://localhost:8080/api/admin/users/{0}/roles" -f $user.userId) -Headers $headers -ContentType "application/json" -Body $rolesBody | Out-Null

Write-Host "5) Asignar sucursal Matriz..."
$branchesBody = @{ branchIds = @($branch.branchId) } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri ("http://localhost:8080/api/admin/users/{0}/branches" -f $user.userId) -Headers $headers -ContentType "application/json" -Body $branchesBody | Out-Null

Write-Host "6) Login como cajero y llamar /api/me..."
$cajeroBody = @{
  email    = $cajeroEmail
  password = $cajeroPassword
} | ConvertTo-Json
$cajeroLogin = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $cajeroBody
$cajeroToken = $cajeroLogin.accessToken
$me = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/me" -Headers @{ Authorization = "Bearer $cajeroToken" }
$me | ConvertTo-Json -Depth 6

Write-Host ""
Write-Host "Query auditoria sugerida (psql):"
Write-Host "select action_code, created_at from audit_events order by created_at desc limit 20;"
