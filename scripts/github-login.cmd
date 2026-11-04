@echo off
setlocal
"%ProgramFiles%\Git\mingw64\bin\git-credential-manager.exe" github login --username Revo1999 --device
if errorlevel 1 exit /b %errorlevel%
"%ProgramFiles%\Git\cmd\git.exe" -C "%~dp0.." ls-remote origin
