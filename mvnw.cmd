@echo off
@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Windows (Mael Especieros Backend)
@REM ----------------------------------------------------------------------------

setlocal enabledelayedexpansion

@REM Ensure JAVA_HOME is set to JDK 21
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-21.0.13" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.13"
    ) else if exist "C:\Program Files\Java\latest\jdk-21" (
        set "JAVA_HOME=C:\Program Files\Java\latest\jdk-21"
    ) else (
        for /d %%D in ("C:\Program Files\Java\jdk-21*") do (
            if exist "%%D\bin\javac.exe" set "JAVA_HOME=%%D"
        )
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

if not defined MAVEN_OPTS (
    set "MAVEN_OPTS=-Djava.net.preferIPv4Stack=true -Dhttps.protocols=TLSv1.2,TLSv1.3"
) else (
    set "MAVEN_OPTS=-Djava.net.preferIPv4Stack=true -Dhttps.protocols=TLSv1.2,TLSv1.3 %MAVEN_OPTS%"
)


@REM Detect Maven
set "MAVEN_CMD="
if exist "%USERPROFILE%\.m2\wrapper\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MAVEN_CMD=%USERPROFILE%\.m2\wrapper\apache-maven-3.9.9\bin\mvn.cmd"
) else if exist "%USERPROFILE%\.m2\wrapper\apache-maven-3.9.8\bin\mvn.cmd" (
    set "MAVEN_CMD=%USERPROFILE%\.m2\wrapper\apache-maven-3.9.8\bin\mvn.cmd"
) else (
    for /d %%M in ("%USERPROFILE%\.m2\wrapper\apache-maven-*") do (
        if exist "%%M\bin\mvn.cmd" set "MAVEN_CMD=%%M\bin\mvn.cmd"
    )
)

if not defined MAVEN_CMD (
    for /r "%USERPROFILE%\.m2\wrapper\dists" %%F in (mvn.cmd) do (
        if exist "%%F" (
            set "MAVEN_CMD=%%F"
            goto :found_maven
        )
    )
)

:found_maven
if defined MAVEN_CMD (
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

