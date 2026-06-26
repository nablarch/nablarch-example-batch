# Goal

NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式のExcelテストデータ（`.xls`）をYAML形式に移行する。
参照: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211

対象は `nablarch-example-batch` リポジトリに存在するNTFのExcelテストデータ全件（現時点で3件、いずれも `.xls`）：
- `src/test/java/com/nablarch/example/app/batch/action/ImportZipCodeFileActionRequestTest.xls`
- `src/test/java/com/nablarch/example/app/batch/form/ZipCodeDataFormatFormTest.xls`
- `src/test/java/com/nablarch/example/app/batch/form/ZipCodeFormTest.xls`

# Acceptance criteria

- `develop` ブランチから作成したブランチで全作業が行われ、`develop` ブランチへのPRが存在する
- 作業前に既存の全テストがパスすることを確認済みである
- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` と `nablarch-testing-converter:1.0.0-SNAPSHOT` の依存関係が追加されている（スコープ: test）
- `src/test/resources/unit-test.xml` に `YamlTestDataParser` コンポーネントと `yamlInterpreters` リストが追加されている
- 3件の `.xls` ファイルがそれぞれ対応するYAMLファイルに変換されている（サンプリングでExcelとYAMLの内容が一致することを確認済み）
- 変換後、元の `.xls` ファイルが全て削除されている
- YAMLテストデータを使った状態で全テストがパスしている
- Excelファイルなしで全テストがパスしていることを確認済みであり、間違えてExcelでパスしていない

# Assumptions

- `nablarch-testing-yaml:1.0.0-SNAPSHOT` と `nablarch-testing-converter:1.0.0-SNAPSHOT` はローカルMavenリポジトリ（`~/.m2`）に存在する（確認済み）
- 参照PRと同様の設定変更（pom.xml、unit-test.xml）でこのリポジトリでも動作する
- テスト実行にはH2インメモリDBを使用し、DBセットアップ済みである
- `nablarch-testing-converter` を使ってXLS→YAML変換が可能である

# Rules

- commit and push every change; one completion marker per task
- 全作業は `develop` ブランチから作成したブランチ上で行う
- PRは `develop` ブランチへ向ける
- テストはExcelファイルが存在しない状態で最終パスを確認する
- git add は明示的なパス指定のみ（`git add -A` / `git add .` 禁止）
- YAML変換後はサンプリングでExcelとYAMLの内容を比較してから削除する
- サンプリング比較で差異が見つかった場合は修正せずユーザーに報告して停止する

# Tasks

### #1: 事前確認 — developブランチチェックアウト・テスト全パス確認

**Purpose**: `develop` ブランチから作業ブランチを作成し、変更前の状態で全テストがパスすることを確認する。

**Prerequisites**: none

**Steps**:

- [x] `develop` ブランチの最新をfetchし、`ntf-yaml-support` ブランチを作成してチェックアウトする
- [x] `mvn test` を実行し、全テストがパスすることを確認する
- [x] 結果を `checks/task-1.md` に記録する（self-check列のみ）
- [x] テスト結果をコミット・プッシュする（steering.mdを除くcheckファイルのみ）
- [x] self-check (OK/NG per completion criterion, record in checks/task-1.md)
- [x] QA expert review (subagent)
- [x] user review

**Completion criteria**:

- `ntf-yaml-support` ブランチが `develop` ブランチから作成されている
- `mvn test` が全テストパスで終了している（BUILD SUCCESS）

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加

**Purpose**: `pom.xml` に2つの依存関係を追加し、`unit-test.xml` に `YamlTestDataParser` の設定を追加する。

**Prerequisites**: #1

**Steps**:

- [ ] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加する
- [ ] `pom.xml` に `nablarch-testing-converter:1.0.0-SNAPSHOT`（test scope）を追加する
- [ ] `unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser` コンポーネントを追加する（参照PR #211の `proman-batch/src/test/resources/unit-test.xml` パッチと同内容）
- [ ] `mvn test -Dsurefire.failIfNoSpecifiedTests=false` などで依存関係が解決できることを確認する
- [ ] self-check (OK/NG per completion criterion, record in checks/task-2.md)
- [ ] QA expert review (subagent)
- [ ] language expert review (subagent)
- [ ] software-engineering expert review (subagent)
- [ ] user review

**Completion criteria**:

- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` と `nablarch-testing-converter:1.0.0-SNAPSHOT` が test スコープで追加されている
- `unit-test.xml` に `yamlInterpreters` リストと `testDataParser` コンポーネント（`YamlTestDataParser`）が追加されている
- `mvn dependency:resolve -Dclassifier=test` が成功する（依存関係解決エラーなし）

### #3: YAML変換 — xlsファイル3件をYAMLに変換

**Purpose**: `nablarch-testing-converter` を使って `.xls` テストデータをYAML形式に変換し、変換内容をサンプリングで確認する。

**Prerequisites**: #2

**Steps**:

- [ ] `nablarch-testing-converter` を使って3件の `.xls` ファイルをYAML変換する
  - `ImportZipCodeFileActionRequestTest.xls`
  - `ZipCodeDataFormatFormTest.xls`
  - `ZipCodeFormTest.xls`
- [ ] 変換後、各YAMLファイルについてExcelの内容と数件サンプリングで比較し、一致を確認する
- [ ] サンプリング比較結果を `checks/task-3.md` に記録する（差異があればユーザーに報告し、修正せずに止める）
- [ ] 変換したYAMLファイルをコミット・プッシュする（Excelはまだ削除しない）
- [ ] self-check (OK/NG per completion criterion, record in checks/task-3.md)
- [ ] QA expert review (subagent)
- [ ] user review

**Completion criteria**:

- 3件の `.xls` ファイルに対応するYAMLファイルが生成されている（参照PRの生成パターンに従ったパス）
- サンプリング比較（各ファイル最低3件のデータ）でExcelとYAMLの内容が一致している（差異があればユーザーに報告してタスク停止、修正は行わない）
- 変換後のYAMLファイルがコミット・プッシュされている

### #4: Excel削除・テスト確認 — xlsファイルを削除してテスト全パス

**Purpose**: `.xls` ファイルを削除し、YAMLテストデータのみの状態で全テストがパスすることを確認する。

**Prerequisites**: #3

**Steps**:

- [ ] 3件の `.xls` ファイルを削除する
- [ ] `mvn test` を実行し、Excelなしの状態で全テストがパスすることを確認する
- [ ] テスト結果（BUILD SUCCESS）を `checks/task-4.md` に記録する
- [ ] xlsファイル削除をコミット・プッシュする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-4.md)
- [ ] QA expert review (subagent)
- [ ] user review

**Completion criteria**:

- NTFのExcelテストデータ（`.xls` / `.xlsx`）が全て削除されている
- Excelファイルが存在しない状態で `mvn test` が全テストパス（BUILD SUCCESS）で完了している

# Decisions

# State

(written by /rn:dn, read and reset to this placeholder by /rn:up. `Status` is `paused` while a
session is suspended — the signal /rn:up and /rn:dn search for — and resets to `not suspended` here,
so only a genuinely suspended session reads `paused`.)

- **Status**: not suspended
- **Date**: 2026-06-26
- **Last completed**: none
- **Next**: #1 事前確認 — developブランチチェックアウト・テスト全パス確認
- **Notes**: nablarch-testing-yaml:1.0.0-SNAPSHOT と nablarch-testing-converter:1.0.0-SNAPSHOT がローカルMavenリポジトリに存在することを確認済み
