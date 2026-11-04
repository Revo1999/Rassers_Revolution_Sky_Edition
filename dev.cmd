@echo off
setlocal
cd /d "%~dp0"
if exist ".local\java-home.txt" set /p JAVA_HOME=<".local\java-home.txt"
if not defined JAVA_HOME (
  echo Set JAVA_HOME to a Java 21 JDK, or run scripts\configure-java.ps1.
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
call gradlew.bat %*
exit /b %errorlevel%