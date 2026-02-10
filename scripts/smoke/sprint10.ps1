Write-Host "[SPR-010] Smoke Variantes"
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

Write-Host "2) Obtener UoM base..."
$uoms = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/admin/catalog/uoms" -Headers $headers
$uom = $uoms | Where-Object { $_.code -eq "UNIDAD" } | Select-Object -First 1
if (-not $uom -and $uoms.Count -gt 0) {
  $uom = $uoms[0]
}
if (-not $uom) {
  Write-Host "No hay UoM disponibles."
  exit 1
}

$suffix = (Get-Date).ToString("HHmmss")

Write-Host "3) Crear producto..."
$productBody = @{
  name     = "Producto Var $suffix"
  sku      = "SKU_VAR_$suffix"
  uomId    = $uom.id
  isActive = $true
} | ConvertTo-Json
$product = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/products" -Headers $headers -ContentType "application/json" -Body $productBody

Write-Host "4) Crear variante con barcode..."
$variantBarcode = "VBAR_$suffix"
$variantBody = @{
  name       = "Variante $suffix"
  barcode    = $variantBarcode
  attributes = @{ color = "rojo"; voltaje = "110v" }
  isActive   = $true
} | ConvertTo-Json
$variant = Invoke-RestMethod -Method Post -Uri ("http://localhost:8080/api/admin/products/{0}/variants" -f $product.id) -Headers $headers -ContentType "application/json" -Body $variantBody
$variant | ConvertTo-Json -Depth 5

Write-Host "5) Lookup por barcode de variante..."
$lookup = Invoke-RestMethod -Method Get -Uri ("http://localhost:8080/api/admin/products/lookup?term={0}" -f $variantBarcode) -Headers $headers
$lookup | ConvertTo-Json -Depth 5
