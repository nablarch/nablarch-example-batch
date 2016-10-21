#!/bin/sh

####################################################################################
#  Script name  : file_delete.sh
#  Description  : 都度起動バッチ
#                 Javaで実装された単実行型バッチプログラムを実行する
#  Server type  : Batch Server
#  User         : batchuser
#  Date         : 2016/2/4
#  Returns      : 0   正常終了
#               : 1   異常終了
####################################################################################

### 引数の数が2以外の場合、バッチを実行せずに終了する ###
if [ $# -ne 2 ]
then
    echo "requestPathとdiConfigを入力してください。"
    exit 1
fi

### 実行オプション設定 ###
NAB_OPT="-classpath ./*;./lib/*"

### JAVA_HOMEを設定 ###
JAVA_HOME_PATH=${JAVA_HOME}

### Mainクラス完全修飾名 ###
MAIN="nablarch.fw.launcher.Main"

### Mainクラス引数を設定 ###
MAIN_ARGS="-requestPath $1 -diConfig $2 -userId 105"

### バッチ実行 ###
"${JAVA_HOME_PATH}/bin/java" ${NAB_OPT} ${MAIN} ${MAIN_ARGS}

### 処理終了ログの出力 ###
EXIT_CODE=${?}
exit ${EXIT_CODE}