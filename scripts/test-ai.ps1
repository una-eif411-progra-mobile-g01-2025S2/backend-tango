# Script para probar el endpoint de IA con OpenRouter (DeepSeek R1 Free)
param(
    [int]$Port = 8080,
    [string]$UserId = "123e4567-e89b-12d3-a456-426614174000"
)
$userId = $UserId
$url = "http://localhost:$Port/api/v1/ai-advisor/advice/$userId"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PROBANDO IA CON OPENROUTER (GRATIS)" -ForegroundColor Cyan
Write-Host "  Modelo: DeepSeek R1 Free" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Endpoint: $url" -ForegroundColor Yellow
Write-Host "Enviando peticion...`n" -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri $url -Method GET -TimeoutSec 90
    Write-Host "RESPUESTA EXITOSA`n" -ForegroundColor Green
    Write-Host "CONSEJO DE LA IA:" -ForegroundColor Cyan
    Write-Host "----------------------------------------" -ForegroundColor Gray
    Write-Host $response.advice -ForegroundColor White
    Write-Host "----------------------------------------" -ForegroundColor Gray
    Write-Host "`nTimestamp: $($response.timestamp)" -ForegroundColor Gray
} catch {
    Write-Host "❌ ERROR AL LLAMAR AL ENDPOINT`n" -ForegroundColor Red
    Write-Host "Mensaje: $($_.Exception.Message)" -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "`nDetalles del servidor:" -ForegroundColor Yellow
        Write-Host $responseBody -ForegroundColor White
    }
}

Write-Host "`n========================================`n" -ForegroundColor Cyan
