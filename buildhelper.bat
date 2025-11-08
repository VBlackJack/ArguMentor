@echo off
REM ============================================================================
REM ArguMentor - Build Helper Menu
REM ============================================================================
REM Interactive menu for common build tasks
REM ============================================================================

:menu
cls
echo ============================================================================
echo              ArguMentor - Build Helper Menu
echo ============================================================================
echo.
echo  Setup:
echo  ------
echo  S. Setup Environment (Install all dependencies)
echo.
echo  Build Options:
echo  --------------
echo  1. Debug Build (APK for testing)
echo  2. Release Build (Optimized APK)
echo  3. Run Unit Tests
echo  4. Run Instrumented Tests (requires device/emulator)
echo  5. Full Build (Clean + Debug + Tests)
echo.
echo  Maintenance:
echo  ------------
echo  6. Clean Build (Quick)
echo  7. Deep Clean (Caches + IDE)
echo  8. Nuclear Clean (Full reset)
echo.
echo  Analysis:
echo  ---------
echo  9. Check Code Quality (Detekt)
echo  10. View Last Error Report
echo  11. Copy AI Prompt to Clipboard
echo.
echo  Other:
echo  ------
echo  0. Exit
echo.
echo ============================================================================
echo.

set /p "CHOICE=Select option (S, 0-11): "

if /i "%CHOICE%"=="S" goto :option_setup
if "%CHOICE%"=="1" goto :option_debug
if "%CHOICE%"=="2" goto :option_release
if "%CHOICE%"=="3" goto :option_test
if "%CHOICE%"=="4" goto :option_instrumented
if "%CHOICE%"=="5" goto :option_full
if "%CHOICE%"=="6" goto :option_clean_quick
if "%CHOICE%"=="7" goto :option_clean_deep
if "%CHOICE%"=="8" goto :option_clean_nuclear
if "%CHOICE%"=="9" goto :option_detekt
if "%CHOICE%"=="10" goto :option_view_error
if "%CHOICE%"=="11" goto :option_copy_prompt
if "%CHOICE%"=="0" goto :exit

echo.
echo Invalid option. Please try again.
timeout /t 2 >nul
goto :menu

:option_setup
    echo.
    echo Running environment setup...
    echo This will install all required dependencies:
    echo - Java JDK 17
    echo - Android SDK
    echo - Gradle wrapper
    echo.
    pause
    call setup.bat
    pause
    goto :menu

:option_debug
    echo.
    echo Running debug build...
    call autobuild.bat debug
    pause
    goto :menu

:option_release
    echo.
    echo Running release build...
    call autobuild.bat release
    pause
    goto :menu

:option_test
    echo.
    echo Running unit tests...
    call autobuild.bat test
    pause
    goto :menu

:option_instrumented
    echo.
    echo Running instrumented tests...
    echo Make sure you have a device/emulator connected!
    echo.
    pause
    call gradlew.bat connectedAndroidTest --stacktrace
    pause
    goto :menu

:option_full
    echo.
    echo Running full build (Clean + Debug + Tests)...
    call autobuild.bat all
    pause
    goto :menu

:option_clean_quick
    echo.
    call clean.bat quick
    pause
    goto :menu

:option_clean_deep
    echo.
    call clean.bat deep
    pause
    goto :menu

:option_clean_nuclear
    echo.
    call clean.bat nuclear
    pause
    goto :menu

:option_detekt
    echo.
    echo Running code quality analysis (Detekt)...
    call gradlew.bat detekt
    echo.
    echo Report generated at: app\build\reports\detekt\
    pause
    goto :menu

:option_view_error
    echo.
    echo Opening last error report...
    if exist "build_logs" (
        dir /b /o-d "build_logs\error_report_*.txt" > temp_list.txt
        set /p LAST_REPORT=<temp_list.txt
        del temp_list.txt
        if exist "build_logs\!LAST_REPORT!" (
            type "build_logs\!LAST_REPORT!"
        ) else (
            echo No error reports found.
        )
    ) else (
        echo No error reports found.
    )
    echo.
    pause
    goto :menu

:option_copy_prompt
    echo.
    if exist "build_logs\ai_prompt.txt" (
        type "build_logs\ai_prompt.txt" | clip
        echo AI prompt copied to clipboard!
        echo You can now paste it into your AI assistant.
    ) else (
        echo No AI prompt found. Run a build first.
    )
    echo.
    pause
    goto :menu

:exit
    echo.
    echo Exiting Build Helper. Goodbye!
    exit /b 0
