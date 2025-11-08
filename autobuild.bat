@echo off
REM ============================================================================
REM ArguMentor - Automatic Build Script with Intelligent Error Handling
REM ============================================================================
REM This script automates the Android build process and generates detailed
REM error reports for AI analysis when build fails.
REM
REM Usage:
REM   autobuild.bat [clean|debug|release|test|all]
REM
REM Options:
REM   clean    - Clean build artifacts
REM   debug    - Build debug APK (default)
REM   release  - Build release APK
REM   test     - Run unit tests
REM   all      - Clean + Build + Test
REM ============================================================================

setlocal enabledelayedexpansion

REM Configuration
set "PROJECT_NAME=ArguMentor"
set "BUILD_TYPE=%1"
set "LOG_DIR=build_logs"
set "ERROR_REPORT=%LOG_DIR%\error_report_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.txt"
set "ERROR_REPORT=%ERROR_REPORT: =0%"
set "AI_PROMPT=%LOG_DIR%\ai_prompt.txt"
set "BUILD_LOG=%LOG_DIR%\build.log"

REM Default to debug if no argument
if "%BUILD_TYPE%"=="" set "BUILD_TYPE=debug"

REM Create log directory
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo ============================================================================
echo %PROJECT_NAME% - Automatic Build System
echo ============================================================================
echo Build Type: %BUILD_TYPE%
echo Start Time: %date% %time%
echo ============================================================================
echo.

REM Check prerequisites
call :check_prerequisites
if errorlevel 1 goto :error_handler

REM Execute build command
call :execute_build
if errorlevel 1 goto :error_handler

REM Success
echo.
echo ============================================================================
echo BUILD SUCCESS!
echo ============================================================================
echo End Time: %date% %time%
echo Build artifacts: app\build\outputs\apk\
echo.
goto :cleanup

:check_prerequisites
    echo [STEP 1/4] Checking prerequisites...
    echo.

    REM Check Java
    echo   - Checking Java...
    java -version >nul 2>&1
    if errorlevel 1 (
        echo     ERROR: Java not found in PATH
        echo     Solution: Install JDK 17 and add to PATH
        set "ERROR_MSG=Java not found. JDK 17 or higher is required."
        exit /b 1
    )
    echo     OK: Java found

    REM Check JAVA_HOME
    echo   - Checking JAVA_HOME...
    if not defined JAVA_HOME (
        echo     WARNING: JAVA_HOME not set
        echo     Attempting to continue anyway...
    ) else (
        echo     OK: JAVA_HOME = %JAVA_HOME%
    )

    REM Check Android SDK
    echo   - Checking Android SDK...
    if not defined ANDROID_HOME (
        if not defined ANDROID_SDK_ROOT (
            echo     WARNING: ANDROID_HOME/ANDROID_SDK_ROOT not set
            echo     Build may fail if SDK not found
        ) else (
            echo     OK: ANDROID_SDK_ROOT = %ANDROID_SDK_ROOT%
        )
    ) else (
        echo     OK: ANDROID_HOME = %ANDROID_HOME%
    )

    REM Check Gradle wrapper
    echo   - Checking Gradle wrapper...
    if not exist "gradlew.bat" (
        echo     ERROR: gradlew.bat not found
        echo     Solution: Run 'gradle wrapper' to generate wrapper
        set "ERROR_MSG=Gradle wrapper not found"
        exit /b 1
    )
    echo     OK: Gradle wrapper found

    echo.
    echo   Prerequisites check completed!
    echo.
    exit /b 0

:execute_build
    echo [STEP 2/4] Executing build command...
    echo.

    REM Execute based on build type
    if /i "%BUILD_TYPE%"=="clean" goto :build_clean
    if /i "%BUILD_TYPE%"=="debug" goto :build_debug
    if /i "%BUILD_TYPE%"=="release" goto :build_release
    if /i "%BUILD_TYPE%"=="test" goto :build_test
    if /i "%BUILD_TYPE%"=="all" goto :build_all

    echo   ERROR: Unknown build type '%BUILD_TYPE%'
    echo   Valid options: clean, debug, release, test, all
    set "ERROR_MSG=Invalid build type: %BUILD_TYPE%"
    exit /b 1

:build_clean
    echo   Executing: gradlew clean
    call gradlew.bat clean > "%BUILD_LOG%" 2>&1
    if errorlevel 1 (
        set "ERROR_MSG=Clean failed"
        set "BUILD_COMMAND=gradlew clean"
        exit /b 1
    )
    echo   Clean completed successfully
    exit /b 0

:build_debug
    echo   Executing: gradlew assembleDebug
    call gradlew.bat assembleDebug --stacktrace > "%BUILD_LOG%" 2>&1
    if errorlevel 1 (
        set "ERROR_MSG=Debug build failed"
        set "BUILD_COMMAND=gradlew assembleDebug"
        exit /b 1
    )
    echo   Debug APK built successfully
    echo   Location: app\build\outputs\apk\debug\app-debug.apk
    exit /b 0

:build_release
    echo   Executing: gradlew assembleRelease
    call gradlew.bat assembleRelease --stacktrace > "%BUILD_LOG%" 2>&1
    if errorlevel 1 (
        set "ERROR_MSG=Release build failed"
        set "BUILD_COMMAND=gradlew assembleRelease"
        exit /b 1
    )
    echo   Release APK built successfully
    echo   Location: app\build\outputs\apk\release\app-release.apk
    exit /b 0

:build_test
    echo   Executing: gradlew test
    call gradlew.bat test --stacktrace > "%BUILD_LOG%" 2>&1
    if errorlevel 1 (
        set "ERROR_MSG=Unit tests failed"
        set "BUILD_COMMAND=gradlew test"
        exit /b 1
    )
    echo   All tests passed
    exit /b 0

:build_all
    echo   Executing: Clean + Build + Test

    call gradlew.bat clean > "%BUILD_LOG%" 2>&1
    if errorlevel 1 (
        set "ERROR_MSG=Clean failed"
        set "BUILD_COMMAND=gradlew clean"
        exit /b 1
    )

    call gradlew.bat assembleDebug --stacktrace >> "%BUILD_LOG%" 2>&1
    if errorlevel 1 (
        set "ERROR_MSG=Build failed"
        set "BUILD_COMMAND=gradlew assembleDebug"
        exit /b 1
    )

    call gradlew.bat test --stacktrace >> "%BUILD_LOG%" 2>&1
    if errorlevel 1 (
        set "ERROR_MSG=Tests failed"
        set "BUILD_COMMAND=gradlew test"
        exit /b 1
    )

    echo   All tasks completed successfully
    exit /b 0

:error_handler
    echo.
    echo ============================================================================
    echo BUILD FAILED!
    echo ============================================================================
    echo Error: !ERROR_MSG!
    echo.
    echo [STEP 3/4] Analyzing error logs...
    echo.

    REM Generate error report
    call :generate_error_report

    REM Generate AI prompt
    call :generate_ai_prompt

    echo.
    echo ============================================================================
    echo ERROR ANALYSIS COMPLETE
    echo ============================================================================
    echo.
    echo Error report saved to: %ERROR_REPORT%
    echo AI prompt saved to:    %AI_PROMPT%
    echo Full build log:        %BUILD_LOG%
    echo.
    echo NEXT STEPS:
    echo 1. Review error report: type %ERROR_REPORT%
    echo 2. Copy AI prompt to clipboard: type %AI_PROMPT% ^| clip
    echo 3. Paste prompt to your AI assistant for analysis
    echo.
    goto :cleanup

:generate_error_report
    echo   Generating error report...

    (
        echo ============================================================================
        echo %PROJECT_NAME% - BUILD ERROR REPORT
        echo ============================================================================
        echo.
        echo Generated: %date% %time%
        echo Build Type: %BUILD_TYPE%
        echo Command: !BUILD_COMMAND!
        echo Error: !ERROR_MSG!
        echo.
        echo ============================================================================
        echo ENVIRONMENT INFORMATION
        echo ============================================================================
        echo.
        echo Java Version:
        java -version 2>&1
        echo.
        echo JAVA_HOME: %JAVA_HOME%
        echo ANDROID_HOME: %ANDROID_HOME%
        echo ANDROID_SDK_ROOT: %ANDROID_SDK_ROOT%
        echo.
        echo ============================================================================
        echo BUILD LOG EXCERPT (Last 100 lines)
        echo ============================================================================
        echo.
        powershell -Command "Get-Content '%BUILD_LOG%' -Tail 100"
        echo.
        echo ============================================================================
        echo COMMON GRADLE ERRORS
        echo ============================================================================
        echo.
        findstr /i /c:"error" /c:"failed" /c:"exception" /c:"cannot find" "%BUILD_LOG%" 2>nul
        echo.
        echo ============================================================================
        echo DEPENDENCY ISSUES
        echo ============================================================================
        echo.
        findstr /i /c:"dependency" /c:"resolution" /c:"download" "%BUILD_LOG%" 2>nul
        echo.
        echo ============================================================================
        echo COMPILATION ERRORS
        echo ============================================================================
        echo.
        findstr /i /c:"compilation" /c:"\.kt:" /c:"\.java:" "%BUILD_LOG%" 2>nul
        echo.
        echo ============================================================================
        echo END OF REPORT
        echo ============================================================================
    ) > "%ERROR_REPORT%"

    echo   Error report generated
    exit /b 0

:generate_ai_prompt
    echo   Generating AI analysis prompt...

    (
        echo I'm building an Android app called ArguMentor using Kotlin, Jetpack Compose, Room, and Hilt.
        echo The build failed with the following error:
        echo.
        echo **Error Message:**
        echo !ERROR_MSG!
        echo.
        echo **Build Command:**
        echo !BUILD_COMMAND!
        echo.
        echo **Environment:**
        echo - OS: Windows
        echo - Build Tool: Gradle
        echo - Language: Kotlin
        echo - UI Framework: Jetpack Compose
        echo - Database: Room
        echo - DI: Hilt
        echo - MinSdk: 24, TargetSdk: 34
        echo.
        echo **Build Log Excerpt (Last 50 lines):**
        echo ```
        powershell -Command "Get-Content '%BUILD_LOG%' -Tail 50"
        echo ```
        echo.
        echo **Key Error Lines:**
        echo ```
        findstr /i /c:"error" /c:"failed" /c:"exception" "%BUILD_LOG%" 2>nul | powershell -Command "$input | Select-Object -First 20"
        echo ```
        echo.
        echo **Questions:**
        echo 1. What is causing this build error?
        echo 2. What are the exact steps to fix it?
        echo 3. Are there any dependency conflicts or version mismatches?
        echo 4. Do I need to update any Gradle configuration files?
        echo 5. Are there any missing permissions or SDK components?
        echo.
        echo Please provide:
        echo - Root cause analysis
        echo - Step-by-step fix instructions
        echo - Any code changes needed
        echo - Prevention tips for future builds
    ) > "%AI_PROMPT%"

    echo   AI prompt generated
    echo.
    echo   To analyze with AI:
    echo   1. Run: type "%AI_PROMPT%" ^| clip
    echo   2. Paste into your AI assistant (Claude, GPT, etc.)
    echo.
    exit /b 0

:cleanup
    echo [STEP 4/4] Cleanup...
    echo.
    REM Keep log files for analysis
    echo   Log files preserved in: %LOG_DIR%\
    echo.
    endlocal
    exit /b 0
