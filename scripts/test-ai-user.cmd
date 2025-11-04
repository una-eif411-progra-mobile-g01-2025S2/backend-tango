@echo off
setlocal

REM Usar usuario real existente en la base de datos
set USER_ID=29c8fd75-066f-4223-9499-44476892d329
set USER_EMAIL=estudiante@test.com

echo ========================================
echo   PROBANDO IA CON USUARIO REAL
echo   Modelo: DeepSeek R1T2 Chimera (OpenRouter)
echo ========================================
echo.

echo Usuario ID: %USER_ID%
echo Usuario: %USER_EMAIL%
echo.

set URL=http://localhost:8080/api/v1/ai-advisor/advice/%USER_ID%

echo Consultando IA...
echo Endpoint: %URL%
echo.
echo ----------------------------------------
echo RESPUESTA DE LA IA:
echo ----------------------------------------

curl -s -X GET "%URL%" > ai_response.json
powershell -Command "$response = Get-Content ai_response.json | ConvertFrom-Json; Write-Host $response.advice -ForegroundColor Cyan"

echo.
echo ----------------------------------------
del ai_response.json 2>nul

echo.
echo ========================================
endlocal
