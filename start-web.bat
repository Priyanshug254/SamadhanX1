@echo off
title SamadhanX Web Portal (Port 5173)
echo ===================================================
echo Starting SamadhanX Web Command Portal on Port 5173...
echo ===================================================
cd /d "%~dp0web"
npm run dev
pause
