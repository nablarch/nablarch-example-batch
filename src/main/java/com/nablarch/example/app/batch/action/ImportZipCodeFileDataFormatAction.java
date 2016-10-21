package com.nablarch.example.app.batch.action;

import com.nablarch.example.app.batch.form.ZipCodeDataFormatForm;
import com.nablarch.example.app.entity.ZipCodeData;
import nablarch.common.dao.UniversalDao;
import nablarch.core.beans.BeanUtil;
import nablarch.core.dataformat.DataRecord;
import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ee.ValidatorUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.action.FileBatchAction;
import nablarch.fw.launcher.CommandLine;
import nablarch.fw.reader.ValidatableFileDataReader;
import nablarch.fw.results.TransactionAbnormalEnd;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * 住所情報ファイルをDBに取り込むバッチ。
 *
 * @author Nabu Rakutaroa
 */
@Published
public class ImportZipCodeFileDataFormatAction extends FileBatchAction {

    /**
     * ファイル名
     */
    private static final String FILE_ID = "importZipCode_by_format.csv";

    /**
     * フォーマットファイル名(拡張子除く)
     */
    private static final String FORMAT_ID = "importZipCode";

    /**
     * 登録が成功した件数
     */
    private static int successCount = 0;

    /**
     * ロガー
     */
    private static final Logger LOG = LoggerManager.get(ImportZipCodeFileDataFormatAction.class);


    /**
     * このメソッドを入力ファイルの名称を返却するようにオーバーライドすることで、
     * FWによって自動的にDataReaderが作成される。
     * @return ファイル名
     */
    @Override
    public String getDataFileName() {
        return FILE_ID;
    }

    /**
     * このメソッドをフォーマット定義ファイルの名称を返却するようにオーバーライドすることで、
     * 指定したフォーマット定義に従って入力ファイルが処理される。
     * @return フォーマットファイル名（拡張子除く）
     */
    @Override
    public String getFormatFileName() {
        return FORMAT_ID;
    }

    /**
     * 初期化処理として、登録対象の既存データを削除する。
     *
     * @param command コマンドライン引数
     * @param context 実行コンテキスト
     */
    @Override
    protected void initialize(CommandLine command, ExecutionContext context) {

        // 登録対象表を削除
        final AppDbConnection conn = DbConnectionContext.getConnection();
        final SqlPStatement statement = conn.prepareStatement("TRUNCATE TABLE ZIP_CODE_DATA");
        statement.executeUpdate();
    }

    /**
     * ファイルレイアウトをバリデーションする。
     *
     * @return バリデータアクション
     */
    @Override
    public ValidatableFileDataReader.FileValidatorAction getValidatorAction() {
        return new ValidatableFileDataReader.FileValidatorAction() {

            /** ファイルレイアウト不正の場合の障害コード */
            private static final String INVALID_FILE_LAYOUT_FAILURE_CODE = "file_layout_invalid";

            /** ファイルのレイアウト不正(レコードの並び順不正)の場合の終了コード */
            private static final int FILE_LAYOUT_ERROR_EXIT_CODE = 100;

            /** ヘッダーレコード */
            private static final String HEADER_RECORD = "1";

            /** データレコード */
            private static final String DATA_RECORD = "2";

            /** 直前に検査されているレコードのタイプ */
            private String preRecordKbn = null;

            /**
             * ヘッダーレコードのバリデーション。
             * <p/>
             * ヘッダーレコードは、1レコード目であること。
             *
             * @param inputData 入力データ
             * @param ctx 実行コンテキスト
             * @return 結果オブジェクト
             */
            public Result doHeader(DataRecord inputData, ExecutionContext ctx) {

                if (preRecordKbn != null) {
                    // 前レコードの値がnull以外の場合は、1レコード目以外のためエラーとする。
                    throw new TransactionAbnormalEnd(FILE_LAYOUT_ERROR_EXIT_CODE,
                            INVALID_FILE_LAYOUT_FAILURE_CODE, inputData.getRecordNumber());
                }
                preRecordKbn = HEADER_RECORD;
                return new Result.Success();
            }

            /**
             * データレコードのバリデーション。
             * <p/>
             * 前レコードのレコード区分は、ヘッダーレコードまたはデータレコードであること。
             *
             * @param inputData 入力データ
             * @param ctx 実行コンテキスト
             * @return 結果オブジェクト
             */
            public Result doData(DataRecord inputData, ExecutionContext ctx) {

                if (!HEADER_RECORD.equals(preRecordKbn)
                        && !DATA_RECORD.equals(preRecordKbn)) {
                    // 前レコードがヘッダー、データで無い場合
                    throw new TransactionAbnormalEnd(FILE_LAYOUT_ERROR_EXIT_CODE,
                            INVALID_FILE_LAYOUT_FAILURE_CODE, inputData.getRecordNumber());
                }
                preRecordKbn = DATA_RECORD;
                return new Result.Success();
            }

            public Result doEnd(DataRecord inputData, ExecutionContext ctx) {
                return new Result.Success();
            }

            /**
             * バリデーション終了時のコールバックメソッド。
             * @param ctx
             */
            @Override
            public void onFileEnd(ExecutionContext ctx) {
                // NOP
            }
        };
    }

    /**
     * ヘッダ用の業務処理メソッド。<p/>
     * 特に処理を行わない場合でも、定義したファイルタイプに対応する業務メソッドは作成する必要がある。
     *
     * @param inputData 一行分のデータ
     * @param ctx       実行コンテキスト
     * @return 結果オブジェクト
     */
    public Result doHeader(DataRecord inputData, ExecutionContext ctx) {
        return new Result.Success();
    }

    /**
     * データのバリデーションと登録を行う。
     * <p/>
     * @param inputData 一行分のデータ
     * @param ctx       実行コンテキスト
     * @return 結果オブジェクト
     */
    public Result doData(DataRecord inputData, ExecutionContext ctx) {

        // inputDataをFormに展開する
        ZipCodeDataFormatForm zipCodeForm = BeanUtil.createAndCopy(ZipCodeDataFormatForm.class, inputData);

        // BeanValidationを実行
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<ZipCodeDataFormatForm>> constraintViolations = validator.validate(zipCodeForm);

        for (ConstraintViolation<ZipCodeDataFormatForm> violation : constraintViolations) {
            // バリデーションエラーの内容をロギングする
            Message message = MessageUtil.createMessage(MessageLevel.WARN,
                    "invalid_data_record", violation.getPropertyPath(),
                    violation.getMessage(), inputData.getRecordNumber());

            LOG.logWarn(message.formatMessage());
        }

        if (constraintViolations.isEmpty()) {
            ZipCodeData data = BeanUtil.createAndCopy(ZipCodeData.class, zipCodeForm);
            UniversalDao.insert(data);
            successCount++;
        }

        return new Result.Success();
    }

    /**
     * エンドレコードの処理。
     * <p/>
     * 成功件数をロギングする。
     *
     * @param inputData 入力データ
     * @param ctx       実行コンテキスト
     * @return 結果オブジェクト
     */
    public Result doEnd(DataRecord inputData, ExecutionContext ctx) {

        writeLog("success.count", successCount);
        return new Result.Success();
    }

}
