@echo off
title Smart Classroom Management System
cd /d "%~dp0"

echo ========================================================
echo   Starting Smart Classroom Management System...
echo ========================================================
echo.

call mvn javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application failed to start or exited with an error.
    echo Please make sure MySQL Server is running.
    echo.
    pause
)

