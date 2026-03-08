@REM Maven Wrapper for Windows
@echo off
set SCRIPT_DIR=%~dp0
set MAVEN_PROJECTBASEDIR=%SCRIPT_DIR%
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar

if not exist "%WRAPPER_JAR%" (
  echo Error: maven-wrapper.jar not found.
  exit /b 1
)

"%JAVA_HOME%\bin\java" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
