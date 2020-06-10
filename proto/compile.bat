@echo off

set DIR=./
set JAVA_TARGET_PATH=..\src\main\java

@echo off

for /R %DIR% %%i in (*.proto) do (
    protoc -I=%%~di%%~pi --java_out=%JAVA_TARGET_PATH% %%i
    echo From %%i To %%~ni.java Successfully!
)
pause