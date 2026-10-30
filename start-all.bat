@echo off
REM ============================================================
REM Start MySQL + Smart Parking Application with Docker Compose
REM ============================================================

set "PATH=%PATH%;C:\Users\anand\AppData\Local\Programs\DockerDesktop\resources\bin"

echo [1/2] Building Spring Boot Application JAR...
call .\mvnw.cmd package -DskipTests

echo.
echo [2/2] Starting MySQL and Application containers...
docker compose up --build -d

echo.
echo ============================================================
echo   Application started successfully!
echo   Swagger UI : http://localhost:8080/swagger-ui/index.html
echo   MySQL Port : 3306 (user: root, password: rootpassword)
echo ============================================================
pause
