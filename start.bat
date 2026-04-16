@echo off
title NETFLIX.sub Launcher
cd /d "%~dp0"

echo ============================================
echo   NETFLIX.sub - Starting servers...
echo ============================================
echo.

REM ---- Check Java ----
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java not found. Please install Java and add it to PATH.
  pause
  exit /b 1
)

REM ---- Check Node/npm ----
where npm >nul 2>&1
if errorlevel 1 (
  echo ERROR: npm not found. Please install Node.js and add it to PATH.
  pause
  exit /b 1
)

REM ---- Find PostgreSQL driver ----
set "DRIVER="
for %%f in (lib\postgresql-*.jar) do set "DRIVER=%%f"
if "%DRIVER%"=="" (
  echo ERROR: lib\postgresql-*.jar not found.
  pause
  exit /b 4
)

REM ---- Build backend ----
echo [1/4] Building backend...
if not exist build\classes mkdir build\classes
javac -cp "%DRIVER%" -d build\classes ^
  shared\src\com\netflixsub\common\*.java ^
  services\auth-service\src\com\netflixsub\auth\*.java ^
  services\user-service\src\com\netflixsub\user\*.java ^
  services\plan-service\src\com\netflixsub\plan\*.java ^
  services\subscription-service\src\com\netflixsub\subscription\*.java ^
  services\payment-service\src\com\netflixsub\payment\*.java ^
  services\notification-service\src\com\netflixsub\notification\*.java ^
  services\catalog-service\src\com\netflixsub\catalog\*.java ^
  services\admin-service\src\com\netflixsub\admin\*.java ^
  model\src\com\netflixsub\model\*.java ^
  services\app\src\com\netflixsub\app\*.java
if errorlevel 1 (
  echo ERROR: Backend build failed.
  pause
  exit /b 1
)
echo       Build successful.

REM ---- Install frontend deps if needed ----
echo [2/4] Checking frontend dependencies...
if not exist frontend\node_modules (
  echo       Installing npm packages...
  cd frontend
  call npm install
  cd ..
)

REM ---- Start backend ----
echo [3/4] Starting backend on http://localhost:8080 ...
if not exist logs mkdir logs
start "NETFLIX.sub Backend" /MIN cmd /c "java -cp build\classes;%DRIVER% com.netflixsub.app.App 2>&1 | tee logs\app.log"

REM ---- Wait for backend to be ready ----
timeout /t 4 /nobreak >nul

REM ---- Start frontend ----
echo [4/4] Starting frontend on http://localhost:5173 ...
start "NETFLIX.sub Frontend" /MIN cmd /c "cd frontend && npx vite"

timeout /t 3 /nobreak >nul

echo.
echo ============================================
echo   Both servers are running!
echo.
echo   Frontend : http://localhost:5173
echo   Backend  : http://localhost:8080
echo.
echo   Admin login: admin@gmail.com / admin@123
echo.
echo   Close this window to keep servers running,
echo   or press any key to stop both servers.
echo ============================================
echo.
pause

REM ---- Cleanup: kill both server windows ----
taskkill /FI "WINDOWTITLE eq NETFLIX.sub Backend*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq NETFLIX.sub Frontend*" /F >nul 2>&1
echo Servers stopped.
timeout /t 2 /nobreak >nul
