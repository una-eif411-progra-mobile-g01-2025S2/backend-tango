@echo off
setlocal
set USER_ID=b1b2b3b4-0000-0000-0000-000000000001
set URL=http://localhost:8080/api/v1/ai-advisor/advice/%USER_ID%

echo ========================================
echo   PROBANDO IA CON OPENROUTER (GRATIS)
echo   Modelo: DeepSeek R1 Free (Usuario 2)
echo ========================================

echo Endpoint: %URL%
echo Enviando peticion...

curl -s -X GET "%URL%"

echo.
echo ========================================
endlocal

