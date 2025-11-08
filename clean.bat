@echo off
REM ============================================================================
REM ArguMentor - Clean Build Script
REM ============================================================================
REM This script performs deep cleaning of build artifacts and caches.
REM
REM Usage: clean.bat [quick|deep|nuclear]
REM
REM Options:
REM   quick   - Clean build/ and .gradle/ (default)
REM   deep    - Quick + delete IDE caches
REM   nuclear - Deep + reinstall Gradle wrapper
REM ============================================================================

setlocal enabledelayedexpansion

set "CLEAN_TYPE=%1"
if "%CLEAN_TYPE%"=="" set "CLEAN_TYPE=quick"

echo ============================================================================
echo ArguMentor - Clean Build Script
echo ============================================================================
echo Clean Type: %CLEAN_TYPE%
echo Start Time: %date% %time%
echo ============================================================================
echo.

if /i "%CLEAN_TYPE%"=="quick" goto :clean_quick
if /i "%CLEAN_TYPE%"=="deep" goto :clean_deep
if /i "%CLEAN_TYPE%"=="nuclear" goto :clean_nuclear

echo ERROR: Invalid clean type '%CLEAN_TYPE%'
echo Valid options: quick, deep, nuclear
goto :end

:clean_quick
    echo [QUICK CLEAN]
    echo.

    echo   Cleaning build directories...
    if exist "build" (
        rd /s /q "build"
        echo   - Removed: build\
    )
    if exist "app\build" (
        rd /s /q "app\build"
        echo   - Removed: app\build\
    )

    echo   Cleaning Gradle cache...
    if exist ".gradle" (
        rd /s /q ".gradle"
        echo   - Removed: .gradle\
    )

    echo   Running Gradle clean...
    call gradlew.bat clean >nul 2>&1
    echo   - Gradle clean completed

    echo.
    echo   Quick clean completed!
    goto :end

:clean_deep
    echo [DEEP CLEAN]
    echo.

    REM Quick clean first
    call :clean_quick

    echo   Cleaning IDE caches...
    if exist ".idea" (
        rd /s /q ".idea"
        echo   - Removed: .idea\
    )
    if exist "*.iml" (
        del /q *.iml
        echo   - Removed: *.iml files
    )
    if exist "app\*.iml" (
        del /q app\*.iml
        echo   - Removed: app\*.iml files
    )

    echo   Cleaning local properties...
    if exist "local.properties" (
        echo   - Keeping: local.properties (contains SDK path)
    )

    echo.
    echo   Deep clean completed!
    goto :end

:clean_nuclear
    echo [NUCLEAR CLEAN - Full Reset]
    echo.
    echo   WARNING: This will delete everything and reinstall Gradle wrapper
    echo.
    set /p "CONFIRM=Are you sure? (Y/N): "
    if /i not "%CONFIRM%"=="Y" (
        echo   Cancelled by user
        goto :end
    )

    echo.
    echo   Performing nuclear clean...

    REM Deep clean first
    call :clean_deep

    echo   Removing Gradle wrapper...
    if exist "gradle" (
        rd /s /q "gradle"
        echo   - Removed: gradle\
    )
    if exist "gradlew.bat" (
        del /q gradlew.bat
        echo   - Removed: gradlew.bat
    )
    if exist "gradlew" (
        del /q gradlew
        echo   - Removed: gradlew
    )

    echo   Removing global Gradle cache...
    if exist "%USERPROFILE%\.gradle\caches" (
        rd /s /q "%USERPROFILE%\.gradle\caches"
        echo   - Removed: %USERPROFILE%\.gradle\caches
    )

    echo.
    echo   Reinstalling Gradle wrapper...
    gradle wrapper --gradle-version 8.2 >nul 2>&1
    if errorlevel 1 (
        echo   ERROR: Failed to reinstall Gradle wrapper
        echo   Please install Gradle manually and run: gradle wrapper
    ) else (
        echo   - Gradle wrapper reinstalled
    )

    echo.
    echo   Nuclear clean completed!
    echo   Run 'gradlew build' to rebuild from scratch
    goto :end

:end
    echo.
    echo ============================================================================
    echo CLEAN COMPLETED
    echo ============================================================================
    echo End Time: %date% %time%
    echo.
    endlocal
    exit /b 0
