@echo off

set SOURCE_FOLDER=./

set JAVA_TARGET_PATH=..\src\main\java

@echo off
for %%i in (*.proto) do (
    protoc -I=%SOURCE_FOLDER% --java_out=%JAVA_TARGET_PATH% %%i
    echo From %%i To %%~ni.java Successfully!
)

pause

