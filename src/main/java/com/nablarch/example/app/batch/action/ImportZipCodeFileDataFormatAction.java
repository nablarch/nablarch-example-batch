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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
    private int successCount;

    /**
     * ロガー
     */
    private static final Logger LOGGER = LoggerManager.get(ImportZipCodeFileDataFormatAction.class);


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
        return new MyFileValidatorAction();
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

            LOGGER.logWarn(message.formatMessage());
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
