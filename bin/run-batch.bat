@echo off

rem ###引数が二つ以外の場合はエラーを出して停止する。###
if "%2"==""  (
    echo requestPathとdiConfigを入力してください。
    exit /b 1
)

if not "%3"=="" (
    echo requestPathとdiConfigを入力してください。
    exit /b 1
)


rem ### 実行オプションを設定する。 ###
set NAB_OPT=-classpath ./*;./lib/*

rem ### JAVA_HOMEを設定 ###
set JAVA_HOME_PATH=%JAVA_HOME%

rem ### Mainクラス完全修飾名 ###
set MAIN=nablarch.fw.launcher.Main

rem ### Mainクラス引数を設定 ###
set MAIN_ARGS=-requestPath %1 -diConfig %2 -userId 105

rem ### バッチ実行 ###
"%JAVA_HOME_PATH%/bin/java" %NAB_OPT% %MAIN% %MAIN_ARGS%

exit /b %ERRORLEVEL%