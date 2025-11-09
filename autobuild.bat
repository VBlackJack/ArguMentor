@echo off
REM ============================================================================
REM ArguMentor - Automatic Build Script with Auto-Dependency Installation
REM ============================================================================
REM This script automates the Android build process, installs missing
REM dependencies, and generates detailed error reports for AI analysis.
REM
REM Usage:
REM   autobuild.bat [clean|debug|release|test|all] [--auto-install]
REM
REM Options:
REM   clean          - Clean build artifacts
REM   debug          - Build debug APK (default)
REM   release        - Build release APK
REM   test           - Run unit tests
REM   all            - Clean + Build + Test
REM   --auto-install - Automatically install missing dependencies
REM ============================================================================

setlocal enabledelayedexpansion

REM Configuration
set "PROJECT_NAME=ArguMentor"
set "BUILD_TYPE=%1"
set "AUTO_INSTALL=0"
set "LOG_DIR=build_logs"
set "ERROR_REPORT=%LOG_DIR%\error_report_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.txt"
set "ERROR_REPORT=%ERROR_REPORT: =0%"
set "AI_PROMPT=%LOG_DIR%\ai_prompt.txt"
set "BUILD_LOG=%LOG_DIR%\build.log"
set "TOOLS_DIR=build_tools"

REM Check for --auto-install flag
if /i "%2"=="--auto-install" set "AUTO_INSTALL=1"
if /i "%1"=="--auto-install" (
    set "AUTO_INSTALL=1"
    set "BUILD_TYPE=debug"
)

REM Default to debug if no argument
if "%BUILD_TYPE%"=="" set "BUILD_TYPE=debug"

REM Create directories
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"

echo ============================================================================
echo %PROJECT_NAME% - Automatic Build System with Auto-Install
echo ============================================================================
echo Build Type: %BUILD_TYPE%
echo Auto-Install: %AUTO_INSTALL%
echo Start Time: %date% %time%
echo ============================================================================
echo.

REM Check and install prerequisites
call :check_and_install_prerequisites
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

:check_and_install_prerequisites
    echo [STEP 1/4] Checking and installing prerequisites...
    echo.

    REM Check Java
    echo   - Checking Java JDK 17+...
    call :check_java
    if errorlevel 1 (
        echo     Java not found or incorrect version
        call :install_java
        if errorlevel 1 exit /b 1
    )
    echo     OK: Java found

    REM Check JAVA_HOME
    echo   - Checking JAVA_HOME...
    if not defined JAVA_HOME (
        echo     WARNING: JAVA_HOME not set
        call :set_java_home
    )
    if defined JAVA_HOME (
        echo     OK: JAVA_HOME = %JAVA_HOME%
    )

    REM Check Android SDK
    echo   - Checking Android SDK...
    call :check_android_sdk
    if errorlevel 1 (
        echo     Android SDK not found
        call :install_android_sdk
        if errorlevel 1 exit /b 1
    )
    echo     OK: Android SDK found

    REM Check Gradle wrapper
    echo   - Checking Gradle wrapper...
    if not exist "gradlew.bat" (
        echo     Gradle wrapper not found
        call :install_gradle_wrapper
        if errorlevel 1 exit /b 1
    )
    echo     OK: Gradle wrapper found

    REM Check Android SDK build-tools and platform
    echo   - Checking Android SDK components...
    call :check_sdk_components
    if errorlevel 1 (
        echo     Some SDK components missing
        call :install_sdk_components
        if errorlevel 1 exit /b 1
    )
    echo     OK: SDK components found

    echo.
    echo   Prerequisites check completed!
    echo.
    exit /b 0

:check_java
    java -version >nul 2>&1
    if errorlevel 1 exit /b 1

    REM Check Java version (need 17+)
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VER=%%i
    )
    set JAVA_VER=%JAVA_VER:"=%
    for /f "tokens=1 delims=." %%a in ("%JAVA_VER%") do set JAVA_MAJOR=%%a
    if %JAVA_MAJOR% LSS 17 (
        echo     Found Java %JAVA_MAJOR%, but need Java 17+
        exit /b 1
    )
    exit /b 0

:install_java
    echo.
    echo   ┌──────────────────────────────────────────────────────────────┐
    echo   │ Java JDK 17+ Required                                        │
    echo   └──────────────────────────────────────────────────────────────┘
    echo.

    if %AUTO_INSTALL%==1 (
        echo   Attempting automatic Java installation...
        call :auto_install_java
        exit /b !errorlevel!
    )

    echo   Java JDK 17 or higher is required to build Android apps.
    echo.
    echo   Options:
    echo   1. Download and install manually (Recommended)
    echo   2. Attempt automatic installation (Experimental)
    echo   3. Skip (build will fail)
    echo.
    set /p "JAVA_CHOICE=Your choice (1-3): "

    if "%JAVA_CHOICE%"=="1" (
        echo.
        echo   Opening download page...
        start https://adoptium.net/temurin/releases/?version=17
        echo.
        echo   After installation:
        echo   1. Close this window
        echo   2. Open a new command prompt
        echo   3. Run this script again
        echo.
        pause
        exit /b 1
    )

    if "%JAVA_CHOICE%"=="2" (
        call :auto_install_java
        exit /b !errorlevel!
    )

    echo   Skipping Java installation - build will likely fail
    exit /b 1

:auto_install_java
    echo   Downloading Temurin JDK 17...

    REM Download using PowerShell
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile '%TOOLS_DIR%\jdk-17.msi'}"

    if not exist "%TOOLS_DIR%\jdk-17.msi" (
        echo   ERROR: Download failed
        echo   Please download manually from https://adoptium.net/
        exit /b 1
    )

    echo   Installing JDK 17...
    msiexec /i "%TOOLS_DIR%\jdk-17.msi" /qn

    echo   Waiting for installation to complete...
    timeout /t 30 /nobreak >nul

    REM Try to find installed Java
    call :set_java_home

    java -version >nul 2>&1
    if errorlevel 1 (
        echo   WARNING: Installation completed but Java not in PATH
        echo   Please restart command prompt or add Java to PATH manually
        exit /b 1
    )

    echo   Java installed successfully!
    exit /b 0

:set_java_home
    REM Try to find Java installation
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
        set "JAVA_HOME=%%i"
        goto :java_home_found
    )
    for /d %%i in ("C:\Program Files\Java\jdk-*") do (
        set "JAVA_HOME=%%i"
        goto :java_home_found
    )
    exit /b 1

:java_home_found
    echo   Setting JAVA_HOME to: !JAVA_HOME!
    setx JAVA_HOME "!JAVA_HOME!" >nul
    exit /b 0

:check_android_sdk
    if defined ANDROID_HOME exit /b 0
    if defined ANDROID_SDK_ROOT exit /b 0

    REM Try to find Android SDK in common locations
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
        setx ANDROID_HOME "!ANDROID_HOME!" >nul
        exit /b 0
    )
    if exist "%USERPROFILE%\AppData\Local\Android\Sdk" (
        set "ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk"
        setx ANDROID_HOME "!ANDROID_HOME!" >nul
        exit /b 0
    )
    exit /b 1

:install_android_sdk
    echo.
    echo   ┌──────────────────────────────────────────────────────────────┐
    echo   │ Android SDK Required                                         │
    echo   └──────────────────────────────────────────────────────────────┘
    echo.

    if %AUTO_INSTALL%==1 (
        echo   Attempting automatic Android SDK installation...
        call :auto_install_android_sdk
        exit /b !errorlevel!
    )

    echo   Android SDK is required to build Android apps.
    echo.
    echo   Options:
    echo   1. Install via Android Studio (Recommended)
    echo   2. Install Command Line Tools only (Lightweight)
    echo   3. Skip (build will fail)
    echo.
    set /p "SDK_CHOICE=Your choice (1-3): "

    if "%SDK_CHOICE%"=="1" (
        echo.
        echo   Opening Android Studio download page...
        start https://developer.android.com/studio
        echo.
        echo   After installation:
        echo   1. Open Android Studio
        echo   2. Go to Tools ^> SDK Manager
        echo   3. Install Android SDK Platform 34 and Build Tools 34.0.0
        echo   4. Close and run this script again
        echo.
        pause
        exit /b 1
    )

    if "%SDK_CHOICE%"=="2" (
        call :auto_install_android_sdk
        exit /b !errorlevel!
    )

    echo   Skipping Android SDK installation - build will fail
    exit /b 1

:auto_install_android_sdk
    echo   Downloading Android Command Line Tools...

    set "SDK_DIR=%LOCALAPPDATA%\Android\Sdk"
    set "CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-win-9477386_latest.zip"

    REM Download
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%CMDLINE_TOOLS_URL%' -OutFile '%TOOLS_DIR%\cmdline-tools.zip'}"

    if not exist "%TOOLS_DIR%\cmdline-tools.zip" (
        echo   ERROR: Download failed
        exit /b 1
    )

    echo   Extracting...
    powershell -Command "Expand-Archive -Path '%TOOLS_DIR%\cmdline-tools.zip' -DestinationPath '%TOOLS_DIR%\cmdline-tools' -Force"

    echo   Setting up Android SDK...
    if not exist "%SDK_DIR%" mkdir "%SDK_DIR%"
    if not exist "%SDK_DIR%\cmdline-tools\latest" mkdir "%SDK_DIR%\cmdline-tools\latest"

    xcopy /E /I /Y "%TOOLS_DIR%\cmdline-tools\cmdline-tools\*" "%SDK_DIR%\cmdline-tools\latest\" >nul

    REM Set environment variable
    set "ANDROID_HOME=%SDK_DIR%"
    setx ANDROID_HOME "%SDK_DIR%" >nul

    echo   Android SDK Command Line Tools installed!
    echo   Location: %SDK_DIR%

    REM Install required SDK components
    call :install_sdk_components

    exit /b 0

:install_gradle_wrapper
    echo   Installing Gradle wrapper...

    REM Check if gradle is in PATH
    gradle --version >nul 2>&1
    if errorlevel 1 (
        echo   ERROR: Gradle not found in PATH
        echo.
        echo   Options:
        echo   1. Install via Chocolatey: choco install gradle
        echo   2. Install via Scoop: scoop install gradle
        echo   3. Download from: https://gradle.org/releases/
        echo.
        if %AUTO_INSTALL%==1 (
            echo   Attempting Chocolatey installation...
            choco install gradle -y >nul 2>&1
            if errorlevel 1 (
                echo   ERROR: Chocolatey installation failed
                echo   Please install Gradle manually
                exit /b 1
            )
        ) else (
            pause
            exit /b 1
        )
    )

    echo   Generating Gradle wrapper...
    gradle wrapper --gradle-version 8.2

    if not exist "gradlew.bat" (
        echo   ERROR: Failed to generate Gradle wrapper
        exit /b 1
    )

    echo   Gradle wrapper installed successfully!
    exit /b 0

:check_sdk_components
    if not defined ANDROID_HOME exit /b 1

    REM Check for platform-tools and build-tools
    if not exist "%ANDROID_HOME%\platform-tools" exit /b 1
    if not exist "%ANDROID_HOME%\build-tools" exit /b 1
    if not exist "%ANDROID_HOME%\platforms\android-34" exit /b 1

    exit /b 0

:install_sdk_components
    if not defined ANDROID_HOME (
        echo   ERROR: ANDROID_HOME not set
        exit /b 1
    )

    echo   Installing required Android SDK components...

    set "SDKMANAGER=%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat"

    if not exist "%SDKMANAGER%" (
        echo   ERROR: sdkmanager not found at %SDKMANAGER%
        exit /b 1
    )

    echo   Accepting licenses...
    echo y | "%SDKMANAGER%" --licenses >nul 2>&1

    echo   Installing platform-tools...
    "%SDKMANAGER%" "platform-tools" >nul 2>&1

    echo   Installing build-tools 34.0.0...
    "%SDKMANAGER%" "build-tools;34.0.0" >nul 2>&1

    echo   Installing Android SDK Platform 34...
    "%SDKMANAGER%" "platforms;android-34" >nul 2>&1

    echo   SDK components installed successfully!
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
    echo   Log files preserved in: %LOG_DIR%\
    echo.
    endlocal
    exit /b 0
