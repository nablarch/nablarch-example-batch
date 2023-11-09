package com.nablarch.example.app.batch.action;

import com.nablarch.example.app.batch.form.ZipCodeForm;
import com.nablarch.example.app.batch.interceptor.ValidateData;
import com.nablarch.example.app.batch.interceptor.ValidateListData;
import com.nablarch.example.app.batch.reader.ListDataReader;
import com.nablarch.example.app.batch.reader.ZipCodeFileReader;
import com.nablarch.example.app.entity.ZipCodeData;
import nablarch.common.dao.UniversalDao;
import nablarch.core.beans.BeanUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.action.BatchAction;
import nablarch.fw.reader.DataBindRecordReader;

import java.util.ArrayList;
import java.util.List;

/**
 * 住所ファイルをDBに登録するバッチクラス。
 * @author Nabu Rakutaro
 */
@Published
public class ImportZipCodeFile2Action extends BatchAction<List<ZipCodeForm>> {

    /**
     * {@link ListDataReader} から渡された複数行の情報をDBに登録する。
     * <p/>
     * メソッド実行時に{@link ValidateData} がインターセプトされるため、
     * このメソッドには常にバリデーション済みの {@code inputData} が引き渡される。
     *
     * @param inputData 一行分の住所情報
     * @param ctx       実行コンテキスト
     * @return 結果オブジェクト
     */
    @Override
    @ValidateListData
    public Result handle(List<ZipCodeForm> inputData, ExecutionContext ctx) {

        List<ZipCodeData> data = new ArrayList<>();
        for (ZipCodeForm form : inputData) {
            data.add(BeanUtil.createAndCopy(ZipCodeData.class, form));
        }

        UniversalDao.batchInsert(data);

        return new Result.Success();
    }

    /**
     * リーダを作成する。
     *
     * @param ctx 実行コンテキスト
     * @return リーダーオブジェクト
     */
    @Override
    public DataReader<List<ZipCodeForm>> createReader(ExecutionContext ctx) {
        ZipCodeFileReader sourceReader = new ZipCodeFileReader();
        return new ListDataReader<>(sourceReader).setChunkSize(5);
    }
}
