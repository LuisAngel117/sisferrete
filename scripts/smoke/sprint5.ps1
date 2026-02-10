Write-Host "[SPR-005] Smoke audit base"
Write-Host ""

$email = $env:SISFERRETE_ADMIN_EMAIL
$password = $env:SISFERRETE_ADMIN_PASSWORD
$totp = $env:SISFERRETE_TOTP

if ([string]::IsNullOrWhiteSpace($email) -or [string]::IsNullOrWhiteSpace($password)) {
  Write-Host "Define SISFERRETE_ADMIN_EMAIL y SISFERRETE_ADMIN_PASSWORD antes de ejecutar."
  exit 1
}

$body = @{
  email    = $email
  password = $password
  totp     = $totp
} | ConvertTo-Json

Write-Host "1) Login..."
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $body

$accessToken = $login.accessToken
Write-Host ("Access token length: {0}" -f $accessToken.Length)

Write-Host ""
Write-Host "2) /api/me..."
$headers = @{ Authorization = "Bearer $accessToken" }
$me = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/me" -Headers $headers
$me | ConvertTo-Json -Depth 6

Write-Host ""
Write-Host "3) Query sugerido (psql):"
Write-Host "select action_code, created_at from audit_events order by created_at desc limit 10;"
