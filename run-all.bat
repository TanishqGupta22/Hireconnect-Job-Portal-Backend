@echo off
setlocal

title HireConnect Starter
color 0A

set ROOT=%~dp0
cd /d "%ROOT%"

echo ==========================================
echo HireConnect Local Development Server
echo ==========================================
echo.

REM ---- Check tools ----
where java >nul 2>&1 || goto nojava
where node >nul 2>&1 || goto nonode
where npm  >nul 2>&1 || goto nonpm
where mvn  >nul 2>&1 || goto nomvn

echo All required tools found.
echo.

set MAVEN_OPTS=-Xmx256m
set SPRING_PROFILES_ACTIVE=local

REM ---- Stop Local Infrastructure ----
echo Stopping local infrastructure to avoid port conflicts (except MySQL)...
taskkill /f /im redis-server.exe >nul 2>&1
taskkill /f /im rabbitmq-server.bat >nul 2>&1
taskkill /f /im erl.exe >nul 2>&1
timeout /t 2 >nul

REM ---- Start Docker Dependencies ----
echo Starting Docker infrastructure (RabbitMQ, Redis)...
docker-compose up -d --remove-orphans
timeout /t 10 >nul

REM ---- Start Services ----

call :run "Eureka Server" "eureka-server" "mvn spring-boot:run"
timeout /t 15 >nul

call :run "API Gateway" "api-gateway" "mvn spring-boot:run"
timeout /t 10 >nul

call :run "Auth Service" "auth-service" "mvn spring-boot:run"
call :run "Profile Service" "profile-service" "mvn spring-boot:run"
call :run "Job Service" "job-service" "mvn spring-boot:run"
call :run "Application Service" "application-service" "mvn spring-boot:run"
call :run "Interview Service" "interview-service" "mvn spring-boot:run"
call :run "Notification Service" "notification-service" "mvn spring-boot:run"
call :run "Subscription Service" "subscription-service" "mvn spring-boot:run"
call :run "Analytics Service" "analytics-service" "mvn spring-boot:run"

call :run "Frontend" "frontend" "npm start"

echo.
echo ==========================================
echo All services launched
echo ==========================================
echo Frontend: http://localhost:4200
echo Gateway : http://localhost:8080
echo Eureka : http://localhost:8761
echo.
pause
exit /b

:run
set NAME=%~1
set FOLDER=%~2
set CMD=%~3

if not exist "%ROOT%%FOLDER%" (
    echo Folder not found: %FOLDER%
    exit /b
)

echo Starting %NAME% ...
start "%NAME%" /D "%ROOT%%FOLDER%" cmd /k "%CMD% -Dspring-boot.run.jvmArguments=\"-Xmx256m\""
exit /b

:nojava
echo Java not found in PATH
pause
exit /b

:nonode
echo Node.js not found in PATH
pause
exit /b

:nonpm
echo NPM not found in PATH
pause
exit /b

:nomvn
echo Maven not found in PATH
pause
exit /b