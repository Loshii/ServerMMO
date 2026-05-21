@echo off
setlocal
set "DIR=%~dp0"
set "CLASSPATH=%DIR%gradle\wrapper\gradle-wrapper.jar"
if not exist "%CLASSPATH%" (
  echo Gradle wrapper JAR no encontrado. Ejecuta gradle wrapper o instala Gradle localmente.
  exit /b 1
)
java -jar "%CLASSPATH%" %*
