@echo off
@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Windows (NUEVOLOOKSR Backend)
@REM ----------------------------------------------------------------------------

setlocal

@REM Ensure JAVA_HOME is set
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-21.0.12.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
    )
)

set "MAVEN_CMD=C:\Users\fer10\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd"
if not exist "%MAVEN_CMD%" (
    set "MAVEN_CMD=C:\Users\fer10\.m2\wrapper\dists\apache-maven-3.9.14\ed7edd442f634ac1c1ef5ba2b61b6d690b5221091f1a8e1123f5fadcc967520d\bin\mvn.cmd"
)

if exist "%MAVEN_CMD%" (
    call "%MAVEN_CMD%" %*
    exit /b %ERRORLEVEL%
)

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    call mvn %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] No se pudo encontrar Maven en su equipo.
exit /b 1
