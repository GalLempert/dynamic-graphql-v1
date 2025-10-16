@ECHO OFF
SETLOCAL
SET BASEDIR=%~dp0
SET WRAPPER_DIR=%BASEDIR%\.mvn\wrapper
SET PROPS_FILE=%WRAPPER_DIR%\maven-wrapper.properties
IF NOT EXIST "%PROPS_FILE%" (
  ECHO Missing maven-wrapper.properties>&2
  EXIT /B 1
)
FOR /F "usebackq tokens=1,* delims==" %%A IN ("%PROPS_FILE%") DO (
  IF "%%A"=="distributionUrl" SET DIST_URL=%%B
)
IF "%DIST_URL%"=="" (
  ECHO distributionUrl not set in %PROPS_FILE%>&2
  EXIT /B 1
)
FOR %%I IN (%DIST_URL%) DO SET DIST_NAME=%%~nI
SET INSTALL_DIR=%WRAPPER_DIR%\%DIST_NAME%
SET ARCHIVE=%WRAPPER_DIR%\%DIST_NAME%.zip
IF NOT EXIST "%INSTALL_DIR%" (
  IF NOT EXIST "%WRAPPER_DIR%" MKDIR "%WRAPPER_DIR%"
  IF NOT EXIST "%ARCHIVE%" (
    WHERE curl >NUL 2>&1
    IF %ERRORLEVEL%==0 (
      curl -fsSL "%DIST_URL%" -o "%ARCHIVE%"
      IF %ERRORLEVEL% NEQ 0 (
        ECHO Failed to download Maven from %DIST_URL%>&2
        EXIT /B 1
      )
    ) ELSE (
      WHERE wget >NUL 2>&1
      IF %ERRORLEVEL%==0 (
        wget -q "%DIST_URL%" -O "%ARCHIVE%"
        IF %ERRORLEVEL% NEQ 0 (
          ECHO Failed to download Maven from %DIST_URL%>&2
          EXIT /B 1
        )
      ) ELSE (
        ECHO Neither curl nor wget is available to download Maven>&2
        EXIT /B 1
      )
    )
  )
  powershell -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%WRAPPER_DIR%' -Force" || (
    ECHO Failed to extract Maven archive>&2
    EXIT /B 1
  )
)
SET MVN_CMD=%INSTALL_DIR%\bin\mvn.cmd
IF NOT EXIST "%MVN_CMD%" SET MVN_CMD=%INSTALL_DIR%\bin\mvn.bat
IF NOT EXIST "%MVN_CMD%" (
  ECHO Maven executable not found in %INSTALL_DIR%>&2
  EXIT /B 1
)
"%MVN_CMD%" %*
