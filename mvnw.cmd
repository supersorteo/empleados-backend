@ECHO OFF
SETLOCAL EnableDelayedExpansion

SET "MVNW_DIR=%~dp0"
IF "%MVNW_DIR:~-1%"=="\" SET "MVNW_DIR=%MVNW_DIR:~0,-1%"
SET "WRAPPER_DIR=%MVNW_DIR%\.mvn\wrapper"
SET "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"
SET "WRAPPER_PROPS=%WRAPPER_DIR%\maven-wrapper.properties"

IF NOT EXIST "%WRAPPER_PROPS%" (
  ECHO [ERROR] No se encontro "%WRAPPER_PROPS%".
  EXIT /B 1
)

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO [INFO] Descargando Maven Wrapper JAR...
  FOR /F "tokens=1,* delims==" %%A IN ('findstr /B /I "wrapperUrl=" "%WRAPPER_PROPS%"') DO SET "WRAPPER_URL=%%B"
  IF "!WRAPPER_URL!"=="" SET "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; New-Item -ItemType Directory -Force '%WRAPPER_DIR%' | Out-Null; Invoke-WebRequest -UseBasicParsing -Uri '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%'"
  IF ERRORLEVEL 1 (
    curl.exe -fL "!WRAPPER_URL!" -o "%WRAPPER_JAR%"
  )
  IF ERRORLEVEL 1 (
    ECHO [ERROR] No se pudo descargar Maven Wrapper desde !WRAPPER_URL!
    EXIT /B 1
  )
)

IF NOT "%JAVA_HOME%"=="" (
  SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET "JAVA_EXE=java"
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MVNW_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
EXIT /B %ERRORLEVEL%
