@echo off
setlocal
set msbuild_verbosity=/v:minimal

if "%1" EQU "help" goto :help

set VERSION=1.0.0.0

call :check_environment

:enter-version-info
echo Enter the version information:
set /p VERSION=  VERSION (DEFAULT %VERSION%)? 

if "%VERSION%" EQU "" set VERSION=1.0.0.0

set /p CONFIRM=Use '%VERSION%' as the version info? (DEFAULT=Y) 
if "%CONFIRM%" EQU "" set CONFIRM=Y
if "%CONFIRM%" NEQ "Y" goto :enter-version-info

msbuild %msbuild_verbosity% ..\build.xml -t:prepare-installer
if ERRORLEVEL 1 goto :done

msbuild %msbuild_verbosity% installer-build.xml -p:VERSION=%VERSION% -t:build-installer
if ERRORLEVEL 1 goto :done

if "%1" NEQ "test" (
  hg commit -m "Advancing version number to %VERSION%..." ..\GlobalAssemblyInfo.cs .\DirectGateway.iss
  if ERRORLEVEL 1 goto :done

  hg tag -m "Tagging CSharp as dotnet-%VERSION%" dotnet-%VERSION%
  if ERRORLEVEL 1 goto :done

  hg archive -r dotnet-%VERSION% -t zip -X certs -X java DirectGateway-%VERSION%-NET35-Source.zip -X .hg*
)

goto :done

@rem determine if msbuild is in the path...
:check_environment
msbuild /? > nul 2> nul
if %ERRORLEVEL% equ 0 goto :eof
call setenv.bat
msbuild /? > nul 2> nul
if %ERRORLEVEL% equ 0 goto :eof
exit /b %ERRORLEVEL%
goto :eof

:help
echo usage: %~n0
echo        builds the installer, the source code archive, advances the version number and tags the repository with the build number
echo.
echo        %~n0 test
echo            builds the installer only 
echo.
echo        %~n0 help
echo            this message
echo.
exit /B

:done
endlocal
goto :eof