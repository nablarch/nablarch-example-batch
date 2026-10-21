# nablarch-example-batch NTF YAML 対応 steering

## Overview
- 目的・背景: NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式のExcelテストデータをYAML形式に移行する（背景資料: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211）。
  - Acceptance criteria 要約: 既存 `ntf-yaml-support` ブランチの履歴を維持したまま、先端の上に develop 一致ツリーのベースラインコミットを積み、ベースラインで全テストPASSを確認 → `pom.xml` に `nablarch-testing-yaml`（test依存）と `nablarch-testing-converter`（ビルドプラグイン）を追加 → `YamlTestDataParser`（`testDataParser`）を全フェーズで有効になる設定ファイルへ追加 → 全テスト用ExcelをYAMLへ変換（サンプリング一致確認）→ Excel削除 → YAMLのみで全テストPASS。
  - 本ブランチには過去の NTF YAML 移行コミット（〜`6981157`）が既に存在するが、指示書に従い task-1〜5 を最初から再実施する（スキップしない）。
- 対象プロジェクト名: nablarch-example-batch
- 指示書: `.rn/ntf-yaml-support/ntf-yaml-support-instructions_batch_V1.6.md`（原本: `/home/k10949/instructions/ntf-yaml-support-instructions_batch_V1.6.md`）
- develop ブランチ: ローカルに `develop` ブランチは無いため、`git fetch origin develop` で最新化した `origin/develop`（`f3b3275`）を develop として扱う。

## Build / Test
- README（「3. アプリケーションのビルド」）より: Java 17 / Maven 3.9.9以降。H2 組み込み DB を使用。
- DB セットアップおよびエンティティクラス生成（gsp-dba-maven-plugin の `generate-ddl` / `execute-ddl` / `generate-entity` / `load-data`）は `generate-resources` フェーズにバインドされており、`mvn package` / `mvn test` に含まれる（個別実行は `mvn generate-resources`）。gsp プロファイル指定は不要。
- gsp プラグインを Java 17 で動かすための JVM オプションは `.mvn/jvm.config` で指定済み。
- テスト実行コマンド: `mvn -B clean test`（clean → generate-resources（gsp）→ compile → test を一括実行）
- converter 実行コマンド（Task #3）: `mvn com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT:convert`（パラメータは pom.xml の plugin `<configuration>` に設定。手順は `/home/k10949/workspace/nablarch-testing-converter/README.md`）

## Scripts
- `.rn/ntf-yaml-support/scripts/` 配下に格納する。作成したスクリプトは用途をここと該当 `checks/task-N.md` に記載する。
- `scripts/XlsDump.java`（Task #3）: サンプリング比較用。`.xls` の全シートの非空セルを TSV（シート/行/列/値）でダンプする。`java -cp ~/.m2/repository/org/apache/poi/poi/3.8/poi-3.8.jar XlsDump.java <file.xls>`（POI 3.8 は NTF と同じ版）
- `scripts/compare_xls_yaml.py`（Task #3）: サンプリング比較用。XlsDump の TSV と converter 出力 YAML を全データ行・全列で突合し、各 YAML から3行（3件未満は全件）をサンプル出力する。生値比較（RAW）と NTF Excel 解釈（NullInterpreter/QuotationTrimmer）適用後の比較を行う。`python3 -I compare_xls_yaml.py <dump.tsv> <yaml_dir>`。結果は `sampling/compare-*.txt`

## Tasks

### #1: ベースライン化（developと同一状態）し、全PASSを確認する
**Purpose**: 既存の `ntf-yaml-support` ブランチ（コミット履歴を維持）上で、過去の移行コミットの有無にかかわらず、ブランチ先端の上にワーキングツリー全体を `develop` と同一内容へ揃えるコミットを1つ新規に積み、その `develop` 同等の状態で全テストがパスすることを確認する。
**Prerequisites**: none
**Steps**:
- [x] 既存の `ntf-yaml-support` ブランチへ切り替える（新規作成なし。先端 `6981157`）
- [x] 過去の移行コミットを巻き戻さず、現在の先端の上に処理を積む（rebase / reset --hard / amend 不使用）
- [x] `origin/develop` の存在を確認し、`git read-tree -u --reset origin/develop` → `git commit` でベースラインコミット `8aa2d9c` を積む（HEAD ツリー = origin/develop ツリーを確認）
- [x] README を確認し、ビルド・テスト手順を把握する（上記 Build / Test）
- [x] steering.md と scripts ディレクトリの初版を作成する
- [x] 本指示書を `.rn/ntf-yaml-support/` へ同名コピーし、`cmp` で原本と同一を確認する
- [x] エンティティクラスを生成する（`mvn -B clean test` 内の generate-resources で gsp-dba の 4 ゴール実行）
- [x] テストを実行し、全テストがパスすることを確認する（Tests run: 12, Failures: 0, Errors: 0 / BUILD SUCCESS）
- [x] 結果を `checks/task-1.md` に記録する
- [x] 変更ファイルをコミットする
- [x] self-check
- [x] QA expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- 既存の `ntf-yaml-support` ブランチ上で作業しており、コミット履歴が維持されている（過去の移行コミットが残っている）
- ブランチの既存先端の上に、ワーキングツリー全体が `develop` と同一内容になる新規コミットが1つ積まれている
- `mvn test` が全テストパスで終了している（BUILD SUCCESS）
- 本指示書のコピーが `.rn/ntf-yaml-support/` 配下に原本と同一内容で配置され、コミットされている

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加
**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）と `nablarch-testing-converter`（`<build><plugins>` のビルドプラグイン）を追加し、`YamlTestDataParser`（`testDataParser`）を testdata.html の設定例どおりに追加する。
**Prerequisites**: #1
**Steps**:
- [x] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加する（`nablarch-testing-junit5` の直後）
- [x] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` を追加する（plugin 直下 `<configuration>` に from=xls / to=yaml / input=output=`${project.basedir}/src/test/java` / overwrite=true。pom.xml の CRLF 改行を維持）
- [x] 実行中にモック等が `testDataParser` 経由でテストデータを読む構成か判定する → 非該当（テストは BatchRequestTestSupport / EntityTestSupport / DbAccessTestSupport のみ。sendSync・応答電文・メッセージングモックの参照なし）
- [x] `YamlTestDataParser` を `src/test/resources/unit-test.xml` に配置する（`test-data.xml` / `test-transaction.xml` の import 直後・`dbInfo` 定義より後。`interpreters` は `dateTimeInterpreter` → `compositeInterpreter` の2つのみを直接 `<list>` 記載）
- [x] `mvn -B -o dependency:resolve`（yaml 依存 test スコープで解決・BUILD SUCCESS）/ `mvn -B -o help:describe -Dplugin=com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT`（`nablarch-testing-converter:convert` ゴール表示・BUILD SUCCESS）で確認する
- [x] 変更したファイルをコミットする
- [x] self-check
- [x] QA expert review (Agent: general-purpose / Opus)
- [x] language expert review (Agent: general-purpose / Opus)
- [x] software-engineering expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている
- `YamlTestDataParser` が全フェーズで有効になる設定ファイルに追加され、`interpreters` が `dateTimeInterpreter` → `compositeInterpreter` の2つのみ
- `mvn dependency:resolve` が成功し、converter の `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換
**Purpose**: `nablarch-testing-converter` で Excel テストデータを YAML に変換し、ソースツリー内に配置してサンプリングで内容を確認する。
**Prerequisites**: #2
**Steps**:
- [x] 対象 Excel ファイルの所在を確認する（`src/test/java` 配下に `.xls` 3件）
- [x] 同名 `.xls` / `.xlsx` の併存有無を確認する（併存なし）
- [x] converter README の手順に従い全 Excel を YAML 変換する（3ファイル→YAML 7本）
- [x] 各 YAML を Excel とサンプリング比較する（全行比較。`testSingleValidation` 2ファイルで `input1` の外側ダブルクォート有無の表記差 各16セルを検出。NTF 解釈後は一致）→ ルールに従い停止・ユーザーへ報告 → ユーザー判断「表記差は一致とみなし続行」
- [x] 結果を `checks/task-3.md` の Sampling Comparison に記録する
- [x] YAML をコミットする（Excel はまだ削除しない）
- [x] self-check
- [x] QA expert review (Agent: general-purpose / Opus)
- [x] user review（差異報告時に実施。表記差は一致とみなし続行の判断）
**Completion criteria**:
- 変換対象の全 Excel に対応する YAML が生成されている
- サンプリング比較で Excel と YAML の内容が一致している
- 変換後の YAML がコミットされている

### #4: Excelを削除する
**Purpose**: 変換対象だった Excel を削除してコミットする。
**Prerequisites**: #3
**Steps**:
- [ ] 変換対象となった Excel ファイルを削除する
- [ ] 削除結果（残存Excel 0件）を `checks/task-4.md` に記録する
- [ ] Excel 削除をコミットする
- [ ] self-check
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] user review
**Completion criteria**:
- 変換対象だった全 Excel ファイルが削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる
**Purpose**: Excel なしで全テストがパスすることを確認する。
**Prerequisites**: #4
**Steps**:
- [ ] `mvn -B clean test` が BUILD SUCCESS になることを確認する
- [ ] テストログで YamlTestDataParser が実際に使われていることを確認する
- [ ] self-check
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] language expert review (Agent: general-purpose / Opus)
- [ ] software-engineering expert review (Agent: general-purpose / Opus)
- [ ] user review
**Completion criteria**:
- `mvn test` が BUILD SUCCESS で終了する（全テストパス、Excel ファイルなし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる
