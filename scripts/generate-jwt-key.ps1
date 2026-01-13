<#
Generates a 256-bit (32 bytes) Base64-encoded JWT secret.
Usage:
  - To print a key: .\generate-jwt-key.ps1
  - To copy to clipboard (if clip.exe available): .\generate-jwt-key.ps1 | clip
  - To automatically update application.properties (DEV ONLY): .\generate-jwt-key.ps1 -SaveToProperties

Warning: Do NOT commit secrets to source control. Use this for local/dev only.
#>
param(
    [switch]$SaveToProperties
)

# Generate 32 random bytes (256 bits)
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$base64 = [Convert]::ToBase64String($bytes)

Write-Host "Generated JWT secret (Base64, 256-bit):`n" -ForegroundColor Green
Write-Host $base64

if ($SaveToProperties) {
    $propPath = Join-Path -Path (Get-Location) -ChildPath "Backend\src\main\resources\application.properties"
    if (Test-Path $propPath) {
        $content = Get-Content $propPath -Raw
        if ($content -match "jwt\.secret=") {
            # Replace existing jwt.secret line
            $newContent = ($content -split "\r?\n") | ForEach-Object {
                if ($_ -match "^jwt\.secret=") { "jwt.secret=$base64" } else { $_ }
            } | Out-String
            $newContent | Set-Content -Path $propPath -Encoding UTF8
        } else {
            Add-Content -Path $propPath -Value "`njwt.secret=$base64"
        }
        Write-Host "Updated $propPath (DEV ONLY). Do NOT commit this file with secrets." -ForegroundColor Yellow
    } else {
        Write-Host "application.properties not found at expected path: $propPath" -ForegroundColor Red
    }
}
