@echo off
REM ============================================================
REM Start only MySQL 8 Database container in Docker
REM ============================================================

set "PATH=%PATH%;C:\Users\anand\AppData\Local\Programs\DockerDesktop\resources\bin"

echo Starting MySQL 8 container...
docker compose up -d mysql

echo.
echo ============================================================
echo   MySQL 8 is running on localhost:3306
echo   Database : parking_management
echo   User     : root
echo   Password : rootpassword
echo.
echo   You can now run your Spring Boot app in your IDE or with:
echo   mvnw.cmd spring-boot:run
echo ============================================================
pause
