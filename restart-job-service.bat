@echo off
set ROOT=%~dp0
echo Stopping job-service...
wmic process where "commandline like '%%job-service%%' and name='java.exe'" call terminate >nul 2>&1
wmic process where "commandline like '%%job-service%%' and name='cmd.exe'" call terminate >nul 2>&1
timeout /t 3 >nul

echo Starting job-service...
cd /d "%ROOT%job-service"
start "Job Service" /B cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=\"-Xmx256m\" > job-service.log 2>&1"
echo Done.
