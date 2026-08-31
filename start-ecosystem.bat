@echo off
title SamadhanX Ecosystem Launcher
echo ================================================================
echo           SamadhanX National GovTech Ecosystem
echo ================================================================
echo Starting Backend (Port 8088) and Web Command Portal (Port 5173)...
echo.

start "SamadhanX Backend" "%~dp0start-backend.bat"
timeout /t 5 /nobreak >nul
start "SamadhanX Web Portal" "%~dp0start-web.bat"

echo ================================================================
echo Both services launched in separate windows!
echo Web Portal: http://localhost:5173
echo Backend API: http://localhost:8088
echo Swagger UI: http://localhost:8088/swagger-ui/index.html
echo ================================================================
pause
