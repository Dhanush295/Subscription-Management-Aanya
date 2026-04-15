@echo off
setlocal
taskkill /FI "WINDOWTITLE eq netflixsub*" /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /R /C:":8080 .*LISTENING"') do (
  taskkill /PID %%a /F >nul 2>&1
)
echo Stopped.
