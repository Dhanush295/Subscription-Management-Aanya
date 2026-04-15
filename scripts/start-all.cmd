@echo off
REM Run the single-port Netflix.sub server on :8080
setlocal
cd /d "%~dp0\.."
if not exist logs mkdir logs

set "DRIVER="
for %%f in (lib\postgresql-*.jar) do set "DRIVER=%%f"
if "%DRIVER%"=="" (
  echo ERROR: lib\postgresql-*.jar not found.
  exit /b 4
)

echo Starting netflix.sub on http://localhost:8080 ...
start "netflixsub" /MIN cmd /c "java -cp build\classes;%DRIVER% com.netflixsub.app.App > logs\app.log 2>&1"
timeout /t 1 /nobreak >nul
echo Server window started (minimized, title netflixsub). Tail logs\app.log for output.
