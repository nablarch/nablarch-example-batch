package com.nablarch.example.app.batch.action;

import nablarch.core.dataformat.DataRecord;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.reader.ValidatableFileDataReader;
import nablarch.fw.results.TransactionAbnormalEnd;

/**
 * ファイルの内容をバリデーションするクラス。
 *
 * @author Nabu Rakutaro
 */
public class MyFileValidatorAction implements ValidatableFileDataReader.FileValidatorAction {

    /** ファイルレイアウト不正の場合の障害コード */
    private static final String INVALID_FILE_LAYOUT_FAILURE_CODE = "file_layout_invalid";

    /** ファイルのレイアウト不正(レコードの並び順不正)の場合の終了コード */
    private static final int FILE_LAYOUT_ERROR_EXIT_CODE = 100;

    /** ヘッダーレコード */
    private static final String HEADER_RECORD = "1";

    /** データレコード */
    private static final String DATA_RECORD = "2";

    /** 直前に検査されているレコードのタイプ */
    private String preRecordKbn;

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

    /**
     * エンドレコードのバリデーションを行う。
     * <p>
     * エンドレコードではバリデーションを行わない。
     *
     * @param inputData 入力データ
     * @param ctx 実行コンテキスト
     * @return 結果オブジェクト
     */
    public Result doEnd(DataRecord inputData, ExecutionContext ctx) {
        return new Result.Success();
    }

    /**
     * バリデーション終了時のコールバックメソッド。
     * @param ctx 実行コンテキスト
     */
    @Override
    public void onFileEnd(ExecutionContext ctx) {
        // NOP
    }
}
