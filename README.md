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

#### 3.1. データベースのセットアップ及びエンティティクラスの作成
まず、データベースのセットアップ及びエンティティクラスの作成を行います。以下のコマンドを実行してください。

    $cd nablarch-example-batch
    $mvn -P gsp generate-resources
    
実行に成功すると、以下のようなログがコンソールに出力され、nablarch-example-batchディレクトリの下にgsp-targetディレクトリが作成されます。

    (中略)
    [INFO] --- gsp-dba-maven-plugin:3.2.0:export-schema (default-cli) @ nablarch-example-batch ---
    [INFO] PUBLICスキーマのExportを開始します。:c:\example\nablarch-example-batch\gsp-target\output\PUBLIC.dmp
    [INFO] Building jar: c:\example\nablarch-example-batch\gsp-target\output\nablarch-example-batch-testdata-0.0.1-SNAPSHOT.jar
    [INFO] PUBLICスキーマのExport完了
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    (中略)

#### 3.2. アプリケーションのビルド、依存するライブラリの取得

次に、アプリケーションをビルドします。以下のコマンドを実行してください。

    $mvn clean package
    
実行に成功すると、以下のようなログがコンソールに出力されます。

    (中略)
    [INFO] --- maven-assembly-plugin:2.5.1:single (default) @ nablarch-example-batch ---
    [INFO] Reading assembly descriptor: src/main/assembly/assemble.xml
    [INFO] Building zip: c:\example\nablarch-example-batch\target\nablarch-example-batch-0.0.1-SNAPSHOT-dist.zip
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    (中略)

ビルド後、以下のコマンドを実行し依存するライブラリを取得します。

    $mvn dependency:copy-dependencies
    
ライブラリの取得に成功すると、以下のようなログがコンソールに出力されます。

    (中略)
    [INFO] Copying stax-api-1.0.1.jar to c:\example\nablarch-example-batch\target\dependency\stax-api-1.0.1.jar
    [INFO] Copying nablarch-common-dao-1.3.0.jar to c:\example\nablarch-example-batch\target\dependency\nablarch-common-dao-1.3.0.jar
    [INFO] Copying jaxen-1.1.6.jar to c:\example\nablarch-example-batch\target\dependency\jaxen-1.1.6.jar
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

#### 4.1 コマンドラインから実行する場合
プロジェクトリポジトリで実行したいバッチのコマンドを実行してください。

  PDFファイル削除バッチ

    $java -cp .\target\*;.\target\dependency\* nablarch.fw.launcher.Main -requestPath FileDeleteAction/FileDelete -diConfig classpath:file-delete.xml -userId 105

  PDF読み込みバッチ

    $java -cp .\target\*;.\target\dependency\* nablarch.fw.launcher.Main -requestPath RegistrationPdfFileAction/RegistrationPdfFile -diConfig classpath:registration-pdf-file.xml -userId 105

  データバインドを使用した住所登録バッチ

    $java -cp .\target\*;.\target\dependency\* nablarch.fw.launcher.Main -requestPath ImportZipCodeFileAction/ImportZipCodeFile -diConfig classpath:import-zip-code-file.xml -userId 105

  汎用データフォーマットを使用した住所登録バッチ

    $java -cp .\target\*;.\target\dependency\* nablarch.fw.launcher.Main -requestPath ImportZipCodeFileDataFormatAction/ImportZipCodeFile -diConfig classpath:import-zip-code-file-data-format.xml -userId 105

#### 4.2 eclipseなどから実行する場合
プログラムの引数に以下を指定して実行してください。

  PDFファイル削除バッチ

    -diConfig classpath:./file-delete.xml
    -requestPath FileDeleteAction
    -userId 105

  PDF読み込みバッチ

    -diConfig classpath:./registration-pdf-file.xml
    -requestPath RegistrationPdfFileAction
    -userId 105

  データバインドを使用した住所登録バッチ

    -diConfig classpath:./import-zip-code-file.xml
    -requestPath ImportZipCodeFileAction
    -userId 105

  汎用データフォーマットを使用した住所登録バッチ

    -diConfig classpath:./import-zip-code-file-data-format.xml
    -requestPath ImportZipCodeFileDataFormatAction
    -userId 105

#### 4.3 バッチ・シェルファイルから起動する場合

このExampleには、バッチの起動に使用する以下のファイルが提供されています。
* run-batch.bat
* run-batch.sh

これらを使用してバッチを起動する方法を説明します。


まず、使用するファイルのJAVA_HOME_PATHに、実行環境のJavaのbinディレクトリまでのパスを設定してください。デフォルトでは、環境変数のJAVA_HOMEの値を参照するよう設定しています。


次に、依存ライブラリを含めたアーカイブを生成します。以下のコマンドを実行してください。

    $mvn clean package
    
アーカイブの生成に成功すると、以下のようなログがコンソールに出力され、targetディレクトリに"nablarch-example-app-batch-<バージョン>-dist.zip"が出力されます。

    [INFO] --- maven-assembly-plugin:2.5.1:single (default) @ nablarch-example-batch ---
    [INFO] Reading assembly descriptor: src/main/assembly/assemble.xml
    [INFO] Building zip: c:\example\nablarch-example-batch\target\nablarch-example-batch-0.0.1-SNAPSHOT-dist.zip
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    
次に、出力されたnablarch-example-app-batch-<バージョン>-dist.zipを任意のディレクトリに展開してください。

最後に、バッチもしくはシェルファイルからアプリケーションを起動します。run-batch.bat、run-batch.sh共に第一引数にrequestPathの値、第二引数にdiConfigの値を入力して起動します。
以下、それぞれのコマンド例です。<展開先ディレクトリ>/nablarch-example-app-batch-<バージョン>-dist/nablarch-example-app-batch-<バージョン>に移動後、実行してください。

バッチファイル

* PDFファイル削除バッチ
    
        $run-batch.bat FileDeleteAction/FileDelete classpath:file-delete.xml
    
* PDF読み込みバッチ

        $run-batch.bat RegistrationPdfFileAction/RegistrationPdfFile classpath:registration-pdf-file.xml
    
* データバインドを使用した住所登録バッチ

        $run-batch.bat ImportZipCodeFileAction/ImportZipCodeFile classpath:import-zip-code-file.xml
    
* 汎用データフォーマットを使用した住所登録バッチ

        $run-batch.bat ImportZipCodeFileDataFormatAction/ImportZipCodeFile classpath:import-zip-code-file-data-format.xml

シェルファイル

* PDFファイル削除バッチ
    
        $sh run-batch.sh FileDeleteAction/FileDelete classpath:file-delete.xml
    
* PDF読み込みバッチ

        $sh run-batch.sh RegistrationPdfFileAction/RegistrationPdfFile classpath:registration-pdf-file.xml
    
* データバインドを使用した住所登録バッチ

        $sh run-batch.sh ImportZipCodeFileAction/ImportZipCodeFile classpath:import-zip-code-file.xml
    
* 汎用データフォーマットを使用した住所登録バッチ

        $sh run-batch.sh ImportZipCodeFileDataFormatAction/ImportZipCodeFile classpath:import-zip-code-file-data-format.xml

### 5. DBの確認方法

1. http://www.h2database.com/html/cheatSheet.html からH2をインストールしてください。

2. {インストールフォルダ}/bin/h2.bat を実行してください(コマンドプロンプトが開く)。
  ※h2.bat実行中はExampleアプリケーションからDBへアクセスすることができないため、バッチを実行できません。

3. ブラウザから http://localhost:8082 を開き、以下の情報でH2コンソールにログインしてください。
   JDBC URLの{dbファイルのパス}には、`nablarch_example.mv.db`ファイルの格納ディレクトリまでのパスを指定してください。  
  JDBC URL：jdbc:h2:{dbファイルのパス}/nablarch_example  
  ユーザ名：NABLARCH_EXAMPLE  
  パスワード：NABLARCH_EXAMPLE
