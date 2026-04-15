@echo off
REM Compile all Java sources into build\classes (single executable: com.netflixsub.app.App)
setlocal
cd /d "%~dp0\.."
if not exist build\classes mkdir build\classes

set "DRIVER="
for %%f in (lib\postgresql-*.jar) do set "DRIVER=%%f"
if "%DRIVER%"=="" (
  echo ERROR: lib\postgresql-*.jar not found. Download from https://jdbc.postgresql.org/download/
  exit /b 4
)

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
  echo Build failed.
  exit /b 1
)
echo Build successful.
