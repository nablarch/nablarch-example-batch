package com.nablarch.example.app.batch.reader.iterator;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.util.annotation.Published;

import java.util.Iterator;

/**
 * {@link ObjectMapper}から要素を抜き出して返却するイテレータクラス。
 *
 * @param <E> {@link ObjectMapper}から変換するオブジェクトの型
 * @author Nabu Rakutaro
 */
@Published
public class ObjectMapperIterator<E> implements Iterator<E> {

    /**
     * イテレート対象のマッパ
     */
    private final ObjectMapper<E> mapper;

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

        final E current = form;
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
