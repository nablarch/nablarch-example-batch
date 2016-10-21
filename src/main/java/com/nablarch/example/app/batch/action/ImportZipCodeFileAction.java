package com.nablarch.example.app.batch.action;

import com.nablarch.example.app.batch.form.ZipCodeForm;
import com.nablarch.example.app.batch.interceptor.ValidateData;
import com.nablarch.example.app.batch.reader.ZipCodeFileReader;
import com.nablarch.example.app.entity.ZipCodeData;
import nablarch.common.dao.UniversalDao;

import nablarch.core.beans.BeanUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.action.BatchAction;

/**
 * 住所ファイルをDBに登録するバッチクラス。
 */
@Published
public class ImportZipCodeFileAction extends BatchAction<ZipCodeForm> {

    /**
     * {@link com.nablarch.example.app.batch.reader.ZipCodeFileReader} から渡された一行分の情報をDBに登録する。
     * <p/>
     * メソッド実行時に{@link com.nablarch.example.app.batch.interceptor.ValidateData} がインターセプトされるため、
     * このメソッドには常にバリデーション済みの {@code inputData} が引き渡される。
     *
     * @param inputData 一行分の住所情報
     * @param ctx       実行コンテキスト
     * @return 結果オブジェクト
     */
    @Override
    @ValidateData
    public Result handle(ZipCodeForm inputData, ExecutionContext ctx) {

        ZipCodeData data = BeanUtil.createAndCopy(ZipCodeData.class, inputData);
        UniversalDao.insert(data);

        return new Result.Success();
    }

    /**
     * リーダを作成する。
     *
     * @param ctx 実行コンテキスト
     * @return リーダーオブジェクト
     */
    @Override
    public DataReader<ZipCodeForm> createReader(ExecutionContext ctx) {
        return new ZipCodeFileReader();
    }
}
