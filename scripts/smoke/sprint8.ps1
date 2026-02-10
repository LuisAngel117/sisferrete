Write-Host "[SPR-008] Smoke Catalogo base"
Write-Host ""

$adminEmail = $env:SISFERRETE_ADMIN_EMAIL
$adminPassword = $env:SISFERRETE_ADMIN_PASSWORD

if ([string]::IsNullOrWhiteSpace($adminEmail) -or [string]::IsNullOrWhiteSpace($adminPassword)) {
  Write-Host "Define SISFERRETE_ADMIN_EMAIL y SISFERRETE_ADMIN_PASSWORD."
  exit 1
}

$loginBody = @{
  email    = $adminEmail
  password = $adminPassword
} | ConvertTo-Json

Write-Host "1) Login admin..."
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $loginBody
$token = $login.accessToken
$headers = @{ Authorization = "Bearer $token" }

Write-Host "2) Listar UoM..."
$uoms = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/admin/catalog/uoms" -Headers $headers
$uoms | ConvertTo-Json -Depth 5

$suffix = (Get-Date).ToString("HHmmss")

Write-Host "3) Crear categoría..."
$categoryBody = @{
  code     = "HERRAMIENTAS_$suffix"
  name     = "Herramientas $suffix"
  isActive = $true
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/catalog/categories" -Headers $headers -ContentType "application/json" -Body $categoryBody | ConvertTo-Json -Depth 5

Write-Host "4) Crear marca..."
$brandBody = @{
  code     = "BOSCH_$suffix"
  name     = "Bosch $suffix"
  isActive = $true
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/catalog/brands" -Headers $headers -ContentType "application/json" -Body $brandBody | ConvertTo-Json -Depth 5

Write-Host ""
Write-Host "Verificacion 403: usar usuario SIN CATALOG_MANAGE con GET /api/admin/catalog/uoms"
