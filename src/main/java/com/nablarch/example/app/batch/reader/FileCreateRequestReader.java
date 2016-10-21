package com.nablarch.example.app.batch.reader;

import java.util.Iterator;

import nablarch.common.dao.DeferredEntityList;
import nablarch.core.message.ApplicationException;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;

import com.nablarch.example.app.entity.FileCreateRequest;

/**
 * ファイル生成リクエストリーダクラス。
 * @author Nabu Rakutaro
 *
 */
@Published
public class FileCreateRequestReader implements DataReader<FileCreateRequest> {
    /** 参照結果レコードのイテレータ */
    private Iterator<FileCreateRequest> records = null;

    /** ファイル生成リクエストのリスト */
    private DeferredEntityList<FileCreateRequest> instance;

    /**
     * コンストラクタ。
     *
     * @param fileCreateRequestList ファイル生成リクエスト
     * @throws ApplicationException 処理対象のファイル生成リクエストが存在しない場合
     */
    @Published
    public FileCreateRequestReader(final DeferredEntityList<FileCreateRequest> fileCreateRequestList) {
        instance = fileCreateRequestList;
        // Iteratorとして保持
        this.records = fileCreateRequestList.iterator();
        if (!this.records.hasNext()) {
            throw new ApplicationException(MessageUtil.createMessage(MessageLevel.ERROR, "error.RegistrationFile.nothing"));
        }
    }

    /**
     * 参照結果のレコードを1行ずつ返却する。
     *
     * @param ctx 実行コンテキスト
     * @return レコードデータを保持するオブジェクト
     */
    public synchronized FileCreateRequest read(ExecutionContext ctx) {
        return this.records.next();
    }

    /**
     * 次のレコードが存在するかどうかを返却する。
     *
     * @param ctx 実行コンテキスト
     * @return 次に読み込むレコードがまだ残っている場合は {@code true}。
     */
    public synchronized boolean hasNext(ExecutionContext ctx) {
        return this.records.hasNext();
    }

    /**
     * リソース解放処理。<br>
     * <br>
     * 読み込み対象のリソースを解放する。
     * @param ctx コンテキスト
     */
    @Override
    public void close(ExecutionContext ctx) {
        if (instance != null) {
            instance.close();
        }
        this.records = null;
    }
}
