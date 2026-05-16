@echo off
title Stop All HireConnect Services
color 0C

echo.
echo ==========================================================
echo 🛑 Stopping All HireConnect Services
echo ==========================================================
echo.

echo 🔄 Stopping all Java processes...
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im javaw.exe >nul 2>&1

echo 🔄 Stopping Node.js processes...
taskkill /f /im node.exe >nul 2>&1

echo 🔄 Stopping Angular CLI...
taskkill /f /im ng.exe >nul 2>&1

echo 🔄 Stopping RabbitMQ...
taskkill /f /im rabbitmq-server.bat >nul 2>&1
taskkill /f /im erl.exe >nul 2>&1

echo 🔄 Stopping Redis...
taskkill /f /im redis-server.exe >nul 2>&1

echo 🔄 Stopping Docker Infrastructure...
docker-compose down

echo 🧹 Cleaning up temporary files...
del /q *.log >nul 2>&1

echo.
echo ✅ All services stopped successfully!
echo.
echo 📊 Memory freed up - Your laptop should now run faster
echo.
pause
