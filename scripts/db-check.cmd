@echo off
REM Compile + run DbCheck against Supabase using credentials from .env
setlocal
cd /d "%~dp0\.."

if not exist build\classes mkdir build\classes

REM Find the Postgres driver jar
set "DRIVER="
for %%f in (lib\postgresql-*.jar) do set "DRIVER=%%f"
if "%DRIVER%"=="" (
  echo ERROR: No postgresql-*.jar found in lib\
  echo        Download postgresql-42.7.4.jar from https://jdbc.postgresql.org/download/ and place it in lib\
  exit /b 4
)

javac -d build\classes shared\src\com\netflixsub\common\DbCheck.java
if errorlevel 1 (
  echo Compile failed.
  exit /b 1
)

java -cp "build\classes;%DRIVER%" com.netflixsub.common.DbCheck
exit /b %errorlevel%
