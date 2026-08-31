@echo off
title SamadhanX Backend (Port 8088)
echo ===================================================
echo Starting SamadhanX Spring Boot Backend on Port 8088...
echo ===================================================
if exist "%~dp0backend\.env" (
    for /f "usebackq tokens=1,* delims==" %%A in ("%~dp0backend\.env") do set "%%A=%%B"
)
if "%SUPABASE_JWT_SECRET%"=="" (
    echo ERROR: Set SUPABASE_JWT_SECRET before starting the backend.
    echo SUPABASE_ISSUER must be https://lldzzybaflefbdsdfauw.supabase.co/auth/v1
    pause
    exit /b 1
)
if "%SUPABASE_ISSUER%"=="" set "SUPABASE_ISSUER=https://lldzzybaflefbdsdfauw.supabase.co/auth/v1"
cd /d "%~dp0backend"
if exist "target\samadhanx-backend-1.0.0-SNAPSHOT.jar" (
    java -jar target\samadhanx-backend-1.0.0-SNAPSHOT.jar
) else (
    .\maven\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
)
pause
