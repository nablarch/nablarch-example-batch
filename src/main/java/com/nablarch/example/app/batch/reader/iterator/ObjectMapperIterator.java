package com.nablarch.example.app.batch.reader.iterator;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.util.annotation.Published;

import java.util.Iterator;

/**
 * {@link ObjectMapper}から要素を抜き出して返却するイテレータクラス。
 *
 * @param <E> {@link ObjectMapper}から変換するオブジェクトの型
 */
@Published
public class ObjectMapperIterator<E> implements Iterator {

    /**
     * イテレート対象のマッパ
     */
    private ObjectMapper<E> mapper = null;

    /**
     * 返却するデータ
     */
    private E form;

    /**
     * {@link ObjectMapper}を引数にObjectMapperIteratorを生成する。
     *
     * @param mapper イテレートするマッパ
     */
    public ObjectMapperIterator(ObjectMapper<E> mapper) {
        this.mapper = mapper;

        // 初回分のデータを読み込む
        form = mapper.read();
    }

    /**
     * 次行があるかどうかを返す。
     *
     * @return 次行がある場合 {@code true}、ない場合{@code false}
     */
    @Override
    public boolean hasNext() {
        return (form != null);
    }

    /**
     * 一行分のデータを返す。
     * <p/>
     *
     * @return 一行分のデータ
     */
    @Override
    public E next() {

        final E current = this.form;
        form = mapper.read();
        return current;
    }

    /**
     * マッパをクローズする。
     */
    public void close() {
        mapper.close();
    }
}
