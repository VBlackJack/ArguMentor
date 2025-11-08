@echo off
REM ============================================================================
REM ArguMentor - Quick Setup Script
REM ============================================================================
REM This script installs all required dependencies for building ArguMentor.
REM It's a one-time setup that prepares your system for Android development.
REM
REM Usage: setup.bat
REM ============================================================================

setlocal enabledelayedexpansion

echo ============================================================================
echo           ArguMentor - Development Environment Setup
echo ============================================================================
echo.
echo This script will install:
echo   - Java JDK 17 (if missing)
echo   - Android Command Line Tools (if missing)
echo   - Android SDK Platform 34
echo   - Android Build Tools 34.0.0
echo   - Gradle Wrapper
echo.
echo Press Ctrl+C to cancel, or
pause

echo.
echo ============================================================================
echo Starting setup...
echo ============================================================================
echo.

REM Create tools directory
if not exist "build_tools" mkdir "build_tools"

REM Check Java
echo [1/5] Checking Java JDK 17+...
java -version >nul 2>&1
if errorlevel 1 (
    echo   Java not found - installing...
    call :install_java
    if errorlevel 1 (
        echo   ERROR: Java installation failed
        goto :error
    )
) else (
    echo   OK: Java already installed
)

REM Check JAVA_HOME
echo.
echo [2/5] Configuring JAVA_HOME...
if not defined JAVA_HOME (
    call :set_java_home
    if errorlevel 1 (
        echo   WARNING: Could not auto-detect JAVA_HOME
        echo   Please set it manually after installation
    ) else (
        echo   OK: JAVA_HOME = !JAVA_HOME!
    )
) else (
    echo   OK: JAVA_HOME = %JAVA_HOME%
)

REM Check Android SDK
echo.
echo [3/5] Checking Android SDK...
if not defined ANDROID_HOME (
    if not defined ANDROID_SDK_ROOT (
        echo   Android SDK not found - installing...
        call :install_android_sdk
        if errorlevel 1 (
            echo   ERROR: Android SDK installation failed
            goto :error
        )
    )
) else (
    echo   OK: Android SDK found at %ANDROID_HOME%
)

REM Install SDK components
echo.
echo [4/5] Installing Android SDK components...
call :install_sdk_components
if errorlevel 1 (
    echo   ERROR: SDK components installation failed
    goto :error
)

REM Setup Gradle wrapper
echo.
echo [5/5] Setting up Gradle wrapper...
if not exist "gradlew.bat" (
    call :install_gradle_wrapper
    if errorlevel 1 (
        echo   WARNING: Gradle wrapper setup failed
        echo   You can run 'gradle wrapper' manually later
    )
) else (
    echo   OK: Gradle wrapper already exists
)

echo.
echo ============================================================================
echo SETUP COMPLETED SUCCESSFULLY!
echo ============================================================================
echo.
echo Your development environment is ready for ArguMentor.
echo.
echo Next steps:
echo   1. Close this command prompt
echo   2. Open a NEW command prompt (to load environment variables)
echo   3. Run: autobuild.bat debug
echo.
echo Environment variables set:
if defined JAVA_HOME echo   JAVA_HOME = %JAVA_HOME%
if defined ANDROID_HOME echo   ANDROID_HOME = %ANDROID_HOME%
echo.
pause
goto :end

:install_java
    echo.
    echo   Downloading Temurin JDK 17...

    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Write-Host 'Downloading JDK...'; try { Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile 'build_tools\jdk-17.msi' -TimeoutSec 300; Write-Host 'Download complete' } catch { Write-Host 'Download failed: $_'; exit 1 }}"

    if not exist "build_tools\jdk-17.msi" (
        echo   ERROR: Download failed
        echo   Please download manually from: https://adoptium.net/temurin/releases/?version=17
        exit /b 1
    )

    echo   Installing JDK 17 (this may take a few minutes)...
    msiexec /i "build_tools\jdk-17.msi" /qn INSTALLDIR="C:\Program Files\Eclipse Adoptium\jdk-17"

    echo   Waiting for installation to complete...
    timeout /t 45 /nobreak >nul

    REM Verify installation
    "C:\Program Files\Eclipse Adoptium\jdk-17\bin\java.exe" -version >nul 2>&1
    if errorlevel 1 (
        echo   ERROR: Java installation verification failed
        exit /b 1
    )

    echo   Java installed successfully!

    REM Add to PATH
    setx PATH "%PATH%;C:\Program Files\Eclipse Adoptium\jdk-17\bin" >nul 2>&1
    set "PATH=%PATH%;C:\Program Files\Eclipse Adoptium\jdk-17\bin"

    exit /b 0

:set_java_home
    REM Try to find Java installation
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17"
        setx JAVA_HOME "!JAVA_HOME!" >nul
        exit /b 0
    )

    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
        set "JAVA_HOME=%%i"
        setx JAVA_HOME "!JAVA_HOME!" >nul
        exit /b 0
    )

    for /d %%i in ("C:\Program Files\Java\jdk-*") do (
        set "JAVA_HOME=%%i"
        setx JAVA_HOME "!JAVA_HOME!" >nul
        exit /b 0
    )

    exit /b 1

:install_android_sdk
    echo.
    echo   Downloading Android Command Line Tools...

    set "SDK_DIR=%LOCALAPPDATA%\Android\Sdk"
    set "CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-win-9477386_latest.zip"

    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Write-Host 'Downloading Android SDK...'; Invoke-WebRequest -Uri '%CMDLINE_TOOLS_URL%' -OutFile 'build_tools\cmdline-tools.zip' -TimeoutSec 300}"

    if not exist "build_tools\cmdline-tools.zip" (
        echo   ERROR: Download failed
        exit /b 1
    )

    echo   Extracting Android SDK...
    powershell -Command "Expand-Archive -Path 'build_tools\cmdline-tools.zip' -DestinationPath 'build_tools\cmdline-tools' -Force"

    echo   Setting up Android SDK directory...
    if not exist "%SDK_DIR%" mkdir "%SDK_DIR%"
    if not exist "%SDK_DIR%\cmdline-tools" mkdir "%SDK_DIR%\cmdline-tools"
    if not exist "%SDK_DIR%\cmdline-tools\latest" mkdir "%SDK_DIR%\cmdline-tools\latest"

    echo   Copying files...
    xcopy /E /I /Y /Q "build_tools\cmdline-tools\cmdline-tools\*" "%SDK_DIR%\cmdline-tools\latest\" >nul

    REM Set environment variable
    set "ANDROID_HOME=%SDK_DIR%"
    setx ANDROID_HOME "%SDK_DIR%" >nul
    setx ANDROID_SDK_ROOT "%SDK_DIR%" >nul

    REM Add to PATH
    setx PATH "%PATH%;%SDK_DIR%\cmdline-tools\latest\bin;%SDK_DIR%\platform-tools" >nul 2>&1

    echo   Android SDK installed successfully!
    echo   Location: %SDK_DIR%

    exit /b 0

:install_sdk_components
    if not defined ANDROID_HOME (
        if defined ANDROID_SDK_ROOT (
            set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
        ) else (
            set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
        )
    )

    set "SDKMANAGER=%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat"

    if not exist "%SDKMANAGER%" (
        echo   ERROR: sdkmanager not found at %SDKMANAGER%
        exit /b 1
    )

    echo   Accepting Android SDK licenses...
    echo y | "%SDKMANAGER%" --licenses 2>nul

    echo   Installing platform-tools...
    "%SDKMANAGER%" "platform-tools" 2>nul

    echo   Installing build-tools 34.0.0...
    "%SDKMANAGER%" "build-tools;34.0.0" 2>nul

    echo   Installing Android SDK Platform 34...
    "%SDKMANAGER%" "platforms;android-34" 2>nul

    echo   Installing additional tools...
    "%SDKMANAGER%" "cmdline-tools;latest" 2>nul

    echo   SDK components installed successfully!
    exit /b 0

:install_gradle_wrapper
    echo   Checking for Gradle...

    gradle --version >nul 2>&1
    if errorlevel 1 (
        echo   Gradle not found in PATH
        echo.
        echo   Installing Gradle via Chocolatey (if available)...
        choco --version >nul 2>&1
        if errorlevel 1 (
            echo   Chocolatey not found - skipping Gradle auto-install
            echo   You can install Gradle manually later
            exit /b 1
        )

        choco install gradle -y
        if errorlevel 1 (
            echo   Chocolatey installation failed
            exit /b 1
        )
    )

    echo   Generating Gradle wrapper...
    gradle wrapper --gradle-version 8.2

    if not exist "gradlew.bat" (
        echo   ERROR: Failed to generate Gradle wrapper
        exit /b 1
    )

    echo   Gradle wrapper created successfully!
    exit /b 0

:error
    echo.
    echo ============================================================================
    echo SETUP FAILED!
    echo ============================================================================
    echo.
    echo Some dependencies could not be installed automatically.
    echo Please install them manually:
    echo.
    echo 1. Java JDK 17+: https://adoptium.net/temurin/releases/?version=17
    echo 2. Android Studio: https://developer.android.com/studio
    echo.
    echo After manual installation, run this script again.
    echo.
    pause
    goto :end

:end
    endlocal
    exit /b 0
