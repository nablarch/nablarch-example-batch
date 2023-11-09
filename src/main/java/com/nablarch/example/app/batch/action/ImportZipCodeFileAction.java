package com.nablarch.example.app.batch.action;

import com.nablarch.example.app.batch.form.ZipCodeForm;
import com.nablarch.example.app.batch.interceptor.ValidateData;
import com.nablarch.example.app.entity.ZipCodeData;
import nablarch.common.dao.UniversalDao;

import nablarch.core.beans.BeanUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.action.DataBindBatchAction;

/**
 * 住所ファイルをDBに登録するバッチクラス。
 * @author Nabu Rakutaro
 */
@Published
public class ImportZipCodeFileAction extends DataBindBatchAction<ZipCodeForm> {

    /**
     * 一行分の情報をDBに登録する。
     * <p/>
     * メソッド実行時に{@link ValidateData} がインターセプトされるため、
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
     * このメソッドを入力データ型を返却するようにオーバーライドすることで、
     * FWによって自動的にDataReaderが作成される。
     *
     * @return 入力データの型
     */
    @Override
    public Class<ZipCodeForm> getInputDataType() {
        return ZipCodeForm.class;
    }

    /**
     * このメソッドを入力ファイルの名称を返却するようにオーバーライドすることで、
     * FWによって自動的にDataReaderが作成される。
     *
     * @return 入力ファイル名
     */
    @Override
    public String getDataFileName() {
        return "importZipCode";
    }

    /**
     * 入力ファイル配置先のベースパスが{@code "input"}でない場合は、
     * このメソッドをオーバーライドして、適切なベースパスを返却するようにする。
     *
     * @return 入力ファイル配置先のベースパス
     */
    @Override
    public String getDataFileDirName() {
        return "csv-input";
    }
}
