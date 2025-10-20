@echo off
setlocal enabledelayedexpansion
echo ========================================
echo   CREANDO DATOS DE PRUEBA PARA IA
echo ========================================
echo.

REM USUARIO FIJO (proporcionado por el usuario)
set USER_ID=29c8fd75-066f-4223-9499-44476892d329
set USER_EMAIL=estudiante@test.com
set USER_NAME=Juan Estudiante
set USER_DEGREE=Ingenieria Informatica
set USER_YEAR=3
set USER_UNIVERSITY=Universidad Nacional
set USER_PASSWORD=12345
REM Extraer el ID del usuario (usando powershell para parsear JSON)
echo [1/4] Usando usuario de prueba fijo...
echo Usuario: %USER_EMAIL%
echo.
echo Nombre: %USER_NAME%
echo Carrera: %USER_DEGREE%
echo Universidad: %USER_UNIVERSITY%
echo Año: %USER_YEAR%
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Programacion Movil\",\"code\":\"EIF411\",\"credits\":4,\"userId\":\"%USER_ID%\"}" > subject1_response.json

for /f "delims=" %%i in ('powershell -Command "(Get-Content subject1_response.json | ConvertFrom-Json).id"') do set SUBJECT1_ID=%%i
echo Materia 1 ID: %SUBJECT1_ID%
echo.

REM Crear materia 2
echo Creando materia: Bases de Datos...
curl -s -X POST "http://localhost:8080/api/v1/subjects" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Bases de Datos\",\"code\":\"EIF206\",\"credits\":4,\"userId\":\"%USER_ID%\"}" > subject2_response.json

for /f "delims=" %%i in ('powershell -Command "(Get-Content subject2_response.json | ConvertFrom-Json).id"') do set SUBJECT2_ID=%%i
echo Materia 2 ID: %SUBJECT2_ID%
echo.

REM Calcular fechas (hoy + 2 dias y hoy + 5 dias)
for /f "tokens=1-3 delims=/ " %%a in ('powershell -Command "Get-Date -Format yyyy-MM-dd"') do set TODAY=%%a
for /f "tokens=1-3 delims=/ " %%a in ('powershell -Command "(Get-Date).AddDays(2).ToString('yyyy-MM-dd')"') do set DATE_2DAYS=%%a
for /f "tokens=1-3 delims=/ " %%a in ('powershell -Command "(Get-Date).AddDays(5).ToString('yyyy-MM-dd')"') do set DATE_5DAYS=%%a

REM Crear tareas
echo [3/4] Creando tareas pendientes...

echo Tarea 1: Entrega de proyecto final (URGENTE - vence en 2 dias)
curl -s -X POST "http://localhost:8080/api/v1/tasks" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Entrega de proyecto final\",\"description\":\"Implementar IA y entregar proyecto completo\",\"priority\":5,\"deadline\":\"%DATE_2DAYS%\",\"status\":\"PENDING\",\"userId\":\"%USER_ID%\",\"subjectId\":\"%SUBJECT1_ID%\"}" > task1_response.json

echo Tarea 2: Quiz de bases de datos (vence en 5 dias)
curl -s -X POST "http://localhost:8080/api/v1/tasks" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Estudiar para quiz de SQL\",\"description\":\"Repasar joins, subconsultas y transacciones\",\"priority\":4,\"deadline\":\"%DATE_5DAYS%\",\"status\":\"PENDING\",\"userId\":\"%USER_ID%\",\"subjectId\":\"%SUBJECT2_ID%\"}" > task2_response.json

echo Tarea 3: Lectura de capitulo 5
curl -s -X POST "http://localhost:8080/api/v1/tasks" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Leer capitulo 5 de Android\",\"description\":\"Leer sobre manejo de estados en Jetpack Compose\",\"priority\":3,\"status\":\"PENDING\",\"userId\":\"%USER_ID%\",\"subjectId\":\"%SUBJECT1_ID%\"}" > task3_response.json

echo Tarea 4: Practica de laboratorio
curl -s -X POST "http://localhost:8080/api/v1/tasks" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Practica de laboratorio\",\"description\":\"Completar ejercicios de normalizacion\",\"priority\":3,\"status\":\"IN_PROGRESS\",\"userId\":\"%USER_ID%\",\"subjectId\":\"%SUBJECT2_ID%\"}" > task4_response.json

echo.
echo [4/4] Limpiando archivos temporales...
del user_response.json subject1_response.json subject2_response.json task1_response.json task2_response.json task3_response.json task4_response.json 2>nul

echo.
del subject1_response.json subject2_response.json task1_response.json task2_response.json task3_response.json task4_response.json 2>nul
echo   DATOS CREADOS EXITOSAMENTE
echo ========================================
echo.
echo Usuario: estudiante@test.com
echo Password: 12345
echo Usuario ID: %USER_ID%
echo Usuario: %USER_EMAIL%
echo Password: %USER_PASSWORD%
echo Tareas creadas: 4 (3 pendientes, 1 en progreso)
echo.
echo Ahora puedes probar la IA con:
echo   test-ai-user.cmd
echo.
echo ========================================
endlocal

