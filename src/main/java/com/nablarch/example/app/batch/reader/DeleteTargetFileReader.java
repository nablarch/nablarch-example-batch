package com.nablarch.example.app.batch.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;

/**
 * 削除対象ファイルリーダクラス。
 * @author Nabu Rakutaro
 *
 */
@Published
public class DeleteTargetFileReader implements DataReader<File> {
    /** 参照結果ファイルのイテレータ */
    private Iterator<Path> files = null;

    /**
     * コンストラクタ。
     *
     * @param targetPath 削除対象フォルダパス
     * @param glob globパターン
     * @throws RuntimeException ファイルの読み込みに失敗した場合
     */
    @Published
    public DeleteTargetFileReader(final Path targetPath, String glob) {
        DirectoryStream<Path> directoryStream;
        try {
            directoryStream = Files.newDirectoryStream(targetPath, glob);
            files = directoryStream.iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 参照結果のファイルを1行づつ返却する。
     *
     * @param ctx 実行コンテキスト
     * @return ファイルデータを保持するオブジェクト
     */
    public synchronized File read(ExecutionContext ctx) {
        if (hasNext(ctx)) {
            return this.files.next().toFile();
        }
        return null;
    }

    /**
     * 次のファイルが存在するかどうかを返却する。
     *
     * @param ctx 実行コンテキスト
     * @return 次に読み込むファイルがまだ残っている場合はtrue。
     */
    public synchronized boolean hasNext(ExecutionContext ctx) {
        return this.files.hasNext();
    }

    /**
     * リソース解放処理。<br>
     * <br>
     * 読み込み対象のリソースを解放する。
     * @param ctx コンテキスト
     */
    @Override
    public void close(ExecutionContext ctx) {
        this.files = null;
    }
}
