Write-Host "[SPR-009] Smoke Productos base"
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
Write-Host ("UoM usada: {0} ({1})" -f $uom.name, $uom.id)

$suffix = (Get-Date).ToString("HHmmss")

Write-Host "3) Crear categoría..."
$categoryBody = @{
  code     = "CATEG_$suffix"
  name     = "Categoria $suffix"
  isActive = $true
} | ConvertTo-Json
$category = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/catalog/categories" -Headers $headers -ContentType "application/json" -Body $categoryBody

Write-Host "4) Crear marca..."
$brandBody = @{
  code     = "MARCA_$suffix"
  name     = "Marca $suffix"
  isActive = $true
} | ConvertTo-Json
$brand = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/catalog/brands" -Headers $headers -ContentType "application/json" -Body $brandBody

Write-Host "5) Crear producto válido..."
$productBody = @{
  name       = "Producto $suffix"
  sku        = "SKU_$suffix"
  barcode    = "BAR_$suffix"
  categoryId = $category.id
  brandId    = $brand.id
  uomId      = $uom.id
  isActive   = $true
} | ConvertTo-Json
$product = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/products" -Headers $headers -ContentType "application/json" -Body $productBody
$product | ConvertTo-Json -Depth 5

Write-Host "6) Buscar por SKU..."
$search = Invoke-RestMethod -Method Get -Uri ("http://localhost:8080/api/admin/products?query={0}&limit=10" -f $product.sku) -Headers $headers
$search | ConvertTo-Json -Depth 5

Write-Host "7) Lookup por SKU..."
$lookup = Invoke-RestMethod -Method Get -Uri ("http://localhost:8080/api/admin/products/lookup?term={0}" -f $product.sku) -Headers $headers
$lookup | ConvertTo-Json -Depth 5

Write-Host "8) Crear producto inválido (sin SKU ni barcode)..."
$invalidBody = @{
  name     = "Invalido $suffix"
  uomId    = $uom.id
  isActive = $true
} | ConvertTo-Json
try {
  Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/products" -Headers $headers -ContentType "application/json" -Body $invalidBody | Out-Null
  Write-Host "ERROR: se esperaba fallo por SKU/barcode"
} catch {
  Write-Host "Fallo esperado:"
  Write-Host $_.Exception.Message
}
