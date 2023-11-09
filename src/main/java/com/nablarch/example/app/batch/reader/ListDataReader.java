package com.nablarch.example.app.batch.reader;

import com.nablarch.example.app.batch.reader.iterator.ObjectMapperIterator;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.reader.DataBindRecordReader;

import java.util.ArrayList;
import java.util.List;

/**
 * 住所ファイルを読み込むためのデータリーダクラス。
 * <p>
 * このデータリーダは、複数行のデータを一度に読み込む。
 * @author Nabu Rakutaro
 */
@Published
public class ListDataReader<T> implements DataReader<List<T>> {


    private final DataReader<T> sourceReader;

    private int chunkSize = 1;

    public ListDataReader(DataReader<T> sourceReader) {
        this.sourceReader = sourceReader;
    }

    /**
     * 業務ハンドラが処理する一行分のデータを返却する。
     *
     * @param ctx 実行コンテキスト
     * @return 一行分のデータ
     */
    @Override
    public List<T> read(ExecutionContext ctx) {
        List<T> forms = new ArrayList<>();

        for (int i = 0; i < chunkSize; i++) {
            if (!sourceReader.hasNext(ctx)) {
                break;
            }
            forms.add(sourceReader.read(ctx));
        }

        return forms;
    }

    /**
     * 次行があるかどうかを返す。
     *
     * @param ctx 実行コンテキスト
     * @return 次行がある場合は {@code true} 、ない場合は {@code false}
     */
    @Override
    public boolean hasNext(ExecutionContext ctx) {
        return sourceReader.hasNext(ctx);
    }

    /**
     * 終了処理。
     * <p/>
     * {@link ObjectMapperIterator#close()} を呼び出す。
     * @param ctx 実行コンテキスト
     */
    @Override
    public void close(ExecutionContext ctx) {
        sourceReader.close(ctx);
    }

    public ListDataReader<T> setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }
}
