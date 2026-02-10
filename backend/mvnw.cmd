@ECHO OFF
SETLOCAL

SET "MAVEN_PROJECTBASEDIR=%~dp0"
SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  ECHO Missing %MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
  EXIT /B 1
)

SET "JAVA_CMD=java"
IF NOT "%JAVA_HOME%"=="" SET "JAVA_CMD=%JAVA_HOME%\bin\java"

"%JAVA_CMD%" %JAVA_OPTS% -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL