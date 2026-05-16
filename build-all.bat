@echo off
echo Building all HireConnect microservices...

REM Services to build
set services=eureka-server api-gateway auth-service profile-service job-service application-service interview-service notification-service subscription-service analytics-service

REM Function to build a service
for %%s in (%services%) do (
    echo Building %%s...
    cd %%s
    
    mvn clean package -DskipTests
    if %ERRORLEVEL% neq 0 (
        echo Failed to build %%s
        exit /b 1
    )
    
    echo %%s built successfully
    cd ..
)

echo All services built successfully!
echo Now you can run: docker-compose up -d
pause
