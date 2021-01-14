# 性能試験実行手順

## テスト内容
郵便番号のCSVを取り込むバッチ(`ImportZipCodeFileAction`)に対して、全国一括のCSVを取り込んでその時間を計測する。

特定のスレッド数で5回試行し、処理時間の中央値をそのスレッド数での処理時間として採用する。
スレッド数は 1, 5 の2パターン実施する。

修正前と修正後でそれぞれテストを行い、性能（処理時間）が劣化していないことを確認する。

## 環境構築

性能検証の環境構築については `terraform/README.md` を参照。

## 実行方法

Terraform で構築した EC2 インスタンス(`performance_batch_ap_instance`)に SSH で接続する。

`run-test.sh` を実行する。

```bash
$ cd /home/ec2-user/nablarch-example-batch/performance_test

$ chmod +x run-test.sh

$ ./run-test.sh
```

初回は `env.sh` ファイルが生成されるので、内容を確認して変数を設定する。

```bash
$ vi /home/ec2-user/nablarch-example-batch/performance_test/env.sh
```

`env.sh` の設定が完了したら、対象を指定して `run-test.sh` を実行する。

```bash
$ cd /home/ec2-user/nablarch-example-batch/performance_test

# 修正前モジュールを実行する
$ ./run-test.sh old

# 修正後モジュールを実行する
$ ./run-test.sh new
```

- `/home/ec2-user/nablarch-example-batch/performance_test/logs` の下にログが出力される。
- `logs` の下は、 `logs/<old|new>/test_<起動時刻>/<スレッド数>/<試行回数>` というルールでディレクトリが作成される
- ログの内容から処理時間を収集する