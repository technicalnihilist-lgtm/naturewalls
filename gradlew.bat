@rem Gradle wrapper launcher for NatureWalls.
@if "%DEBUG%"=="" @echo off
set DIRNAME=%~dp0
if "%JAVA_HOME%"=="" (set JAVA_EXE=java) else (set JAVA_EXE=%JAVA_HOME%\bin\java.exe)
"%JAVA_EXE%" -classpath "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
