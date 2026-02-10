Write-Host "[SPR-011] Smoke Presentaciones"
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

Write-Host "2) Obtener UoM base y venta..."
$uoms = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/admin/catalog/uoms" -Headers $headers
$baseUom = $uoms | Where-Object { $_.code -eq "UNIDAD" } | Select-Object -First 1
if (-not $baseUom -and $uoms.Count -gt 0) {
  $baseUom = $uoms[0]
}
$saleUom = $uoms | Where-Object { $_.code -eq "CAJA" } | Select-Object -First 1
if (-not $saleUom -and $uoms.Count -gt 0) {
  $saleUom = $uoms[0]
}
if (-not $baseUom -or -not $saleUom) {
  Write-Host "No hay UoM disponibles."
  exit 1
}

$suffix = (Get-Date).ToString("HHmmss")

Write-Host "3) Crear producto base..."
$productBody = @{
  name     = "Producto Pack $suffix"
  sku      = "SKU_PACK_$suffix"
  uomId    = $baseUom.id
  isActive = $true
} | ConvertTo-Json
$product = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/products" -Headers $headers -ContentType "application/json" -Body $productBody

Write-Host "4) Crear presentación CAJA..."
$packBarcode = "PACK_$suffix"
$packBody = @{
  saleUomId             = $saleUom.id
  baseUomId             = $baseUom.id
  baseUnitsPerSaleUnit  = 100
  barcode               = $packBarcode
  isDefaultForSale      = $true
  isActive              = $true
} | ConvertTo-Json
$packaging = Invoke-RestMethod -Method Post -Uri ("http://localhost:8080/api/admin/products/{0}/packagings" -f $product.id) -Headers $headers -ContentType "application/json" -Body $packBody
$packaging | ConvertTo-Json -Depth 5

Write-Host "5) Lookup por barcode de presentación..."
$lookup = Invoke-RestMethod -Method Get -Uri ("http://localhost:8080/api/admin/products/lookup?term={0}" -f $packBarcode) -Headers $headers
$lookup | ConvertTo-Json -Depth 5
