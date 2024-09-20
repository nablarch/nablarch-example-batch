nablarch-example-batch
===========================

Nablarchアプリケーションフレームワークを利用して作成したNablarchバッチExampleアプリケーションです。

## 実行手順

### 1.動作環境
実行環境に以下のソフトウェアがインストールされている事を前提とします。
* Java Version : 8
* Maven 3.0.5以降

なお、このアプリケーションはH2 Database Engineを組み込んでいます。別途DBサーバのインストールは必要ありません。

### 2. プロジェクトリポジトリの取得
Gitを使用している場合、アプリケーションを配置したいディレクトリにて「git clone」コマンドを実行してください。
以下、コマンドの例です。

    $mkdir c:\example
    $cd c:\example
    $git clone https://github.com/nablarch/nablarch-example-batch.git

Gitを使用しない場合、最新のタグからzipをダウンロードし、任意のディレクトリへ展開してください。

### 3. アプリケーションのビルド
次に、データベースのセットアップとアプリケーションのビルドを行います。以下のコマンドを実行してください。

    $cd nablarch-example-batch
    $mvn package
    
実行に成功すると、以下のようなログがコンソールに出力されます。

    (中略)
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    (中略)

### 4.アプリケーションの実行

本Exampleでは以下のバッチが実装されています。

* 都度起動バッチ
    * PDFファイル削除バッチ
        * 指定されたフォルダ内のpdfファイルを削除します。
    * データバインドを使用した住所登録バッチ
        * 指定されたフォルダ内の住所情報CSVファイルを、データバインドを使用して読み込み、DBに登録します。
    * 汎用データフォーマットを使用した住所登録バッチ
        * 指定されたフォルダ内の住所情報CSVファイルを、汎用データフォーマットを使用して読み込み、DBに登録します。
* 常駐バッチ
    * PDF読み込みバッチ
        * 指定されたフォルダ内のpdfファイルを読み込んだ後、ファイルを削除します。

#### 4.1 実行コマンド例
プロジェクトリポジトリで実行したいバッチのコマンドを実行してください。

* PDFファイル削除バッチ

    バッチ実行前に `work/test/registration/test/test1.pdf` を `work/registration/tmp` にコピーしてください。

    `test1.pdf` ファイルの更新日時(lastModified)が、nablarch システム日付の前日より前であることを確認してください。

     例：nablarch システム日付が`2019/05/29`の場合、ファイルの更新日時が`2019/05/27`までのものが削除対象となり、`2019/05/28`の場合は削除対象外となります。

      $mvn exec:java -Dexec.mainClass=nablarch.fw.launcher.Main -Dexec.args="'-requestPath' 'FileDeleteAction/FileDelete' '-diConfig' 'classpath:file-delete.xml' '-userId' '105'"

* PDF読み込みバッチ

    バッチ実行前に下記処理を行ってください：

    `mvn gsp-dba:execute-ddl` 及び `mvn gsp-dba:load-data` を実行してDBのデータを初期化してください。

    `work/test/registration/test/test1.pdf` を `work/registration/input` にコピーしてください。

      $mvn exec:java -Dexec.mainClass=nablarch.fw.launcher.Main -Dexec.args="'-requestPath' 'RegistrationPdfFileAction/RegistrationPdfFile' '-diConfig' 'classpath:registration-pdf-file.xml' '-userId' '105'"

* データバインドを使用した住所登録バッチ

      $mvn exec:java -Dexec.mainClass=nablarch.fw.launcher.Main -Dexec.args="'-requestPath' 'ImportZipCodeFileAction/ImportZipCodeFile' '-diConfig' 'classpath:import-zip-code-file.xml' '-userId' '105'"

* 汎用データフォーマットを使用した住所登録バッチ

      $mvn exec:java -Dexec.mainClass=nablarch.fw.launcher.Main -Dexec.args="'-requestPath' 'ImportZipCodeFileDataFormatAction/ImportZipCodeFile' '-diConfig' 'classpath:import-zip-code-file-data-format.xml' '-userId' '105'"
    
なお、 `maven-assembly-plugin` を使用して実行可能 jar の生成を行っているため、以下の手順にて実行することもできる。

1. ``target/application-<version_no>.zip`` を任意のディレクトリに解凍する。
2. 以下のコマンドにて実行する

       $java -jar <1で解凍したディレクトリ名>/nablarch-example-batch-<version_no>.jar <起動に必要な引数(mvn exec:javaの例を参照)>

### 5. DBの確認方法

1. https://www.h2database.com/html/download.html からH2をインストールしてください。  

2. {インストールフォルダ}/bin/h2.bat を実行してください(コマンドプロンプトが開く)。
  ※h2.bat実行中はExampleアプリケーションからDBへアクセスすることができないため、バッチを実行できません。

3. ブラウザから http://localhost:8082 を開き、以下の情報でH2コンソールにログインしてください。
   JDBC URLの{dbファイルのパス}には、`nablarch_example.mv.db`ファイルの格納ディレクトリまでのパスを指定してください。  
  JDBC URL：jdbc:h2:{dbファイルのパス}/nablarch_example  
  ユーザ名：NABLARCH_EXAMPLE  
  パスワード：NABLARCH_EXAMPLE


