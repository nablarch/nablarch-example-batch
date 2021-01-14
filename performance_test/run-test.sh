#!/bin/bash -u

THIS_DIR=$(cd $(dirname $0); pwd)
PROJECT_DIR=$(cd $THIS_DIR/..; pwd)

# 環境設定ファイルの読み込み
ENV_FILE=$THIS_DIR/env.sh

if [ ! -e $ENV_FILE ]; then
    cat << EOS > $ENV_FILE
# RDSのエンドポイント
export RDS_ENDPOINT=changeme

# 実行するスレッド数のパターン
export THREAD_PATTERN="1 5"
# 各スレッド数での試行回数
export LOOP_COUNT=5
EOS

    echo $ENV_FILE が存在しなかったので生成しました。
    echo ファイルを開いて変数を設定してください。
    exit 1
fi

source $ENV_FILE

# 対象の決定 (old or new)
TARGET=$1

if [ "$TARGET" != "old" -a "$TARGET" != "new" ]; then
    echo 第一引数は old または new を指定してください TARGET=$TARGET
    exit 1
fi

# 関数定義
function initializeDatabase() {
    cd $PROJECT_DIR
    mvn generate-resources -Ddb.host=$RDS_ENDPOINT
}

function runBatchInBackground() {
    cd $PROJECT_DIR/target/${TARGET}_batch
    java -jar nablarch-example-batch-5-NEXT-SNAPSHOT.jar \
          -requestPath ImportZipCodeFileAction/ImportZipCodeFile \
          -diConfig classpath:import-zip-code-file.xml \
          -userId 105 &
}

function waitBatchProcess() {
    wait
}

function startJstat() {
  jcmd | sed -n -r 's/([0-9]+) nablarch-example-batch-5-NEXT-SNAPSHOT.jar .*/\1/p' | xargs -I {} jstat -gc {} 1s > ./jstat.log &
}

function filterJstatLog() {
    # 必要な項目だけを抽出して、タブ区切りにして別ファイルに出力
    awk 'BEGIN { OFS = "\t" } { print $6,$8,$13,$14,$15,$16,$17}' ./jstat.log > ./jstat_filtered.log
}

function collectLogs() {
    local LOG_DIR=$1
    local LOG_ZIP_FILE_NAME=logs_`date "+%Y%m%d_%H%M%S"`.zip

    mv ./app.log* $LOG_DIR
    mv ./monitor.log* $LOG_DIR
    mv ./*.log $LOG_DIR
}

# main 処理
echo THREAD_PATTERN=$THREAD_PATTERN
echo LOOP_COUNT=$LOOP_COUNT

# 全国一括ファイルを読み込むように環境変数を設定
export CSV_FILE_NAME=importZipCode_all

OUT_DIR=$THIS_DIR/logs/$TARGET/test_`date "+%Y%m%d_%H%M%S"`
mkdir -p $OUT_DIR

# スレッド（ユーザ）数ループ
for THREAD_NUMBER in $THREAD_PATTERN
do
    THREAD_DIR=$OUT_DIR/$THREAD_NUMBER
    mkdir $THREAD_DIR

    # スレッド数設定
    export NABLARCH_MULTITHREADEXECUTIONHANDLER_THREADCOUNT=$THREAD_NUMBER

    # 試行回ループ
    for i in `seq 1 $LOOP_COUNT`
    do
        WORK_DIR=$THREAD_DIR/$i
        mkdir $WORK_DIR

        STATE="(thread=$THREAD_NUMBER, i=$i)"
        echo ===== Initialize Database $STATE =====
        initializeDatabase

        echo ===== Run Batch $STATE =====
        runBatchInBackground

        echo ===== Start Jstat $STATE =====
        startJstat

        echo ===== Wait Batch Process $STATE =====
        waitBatchProcess

        echo ===== Filter Jstat Log $STATE =====
        filterJstatLog

        echo ===== Collect Application Logs $STATE =====
        collectLogs $WORK_DIR
    done
done
