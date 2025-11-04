@echo off
setlocal
set USER_ID=29c8fd75-066f-4223-9499-44476892d329
set URL=http://localhost:8080/api/v1/ai-advisor/advice/%USER_ID%

echo ========================================
echo   PROBANDO IA CON OPENROUTER (GRATIS)
echo   Modelo: DeepSeek R1 Free
echo ========================================

echo Endpoint: %URL%
echo Enviando peticion...

curl -s -X GET "%URL%"

echo.
echo ========================================
endlocal

