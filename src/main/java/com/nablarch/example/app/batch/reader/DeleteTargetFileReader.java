package com.nablarch.example.app.batch.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private final Iterator<Path> files;

    /**
     * コンストラクタ。
     *
     * @param targetPath 削除対象フォルダパス
     * @param glob globパターン
     * @throws RuntimeException ファイルの読み込みに失敗した場合
     */
    @Published
    public DeleteTargetFileReader(final Path targetPath, String glob) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(targetPath, glob)){
            final List<Path> paths = new ArrayList<>();
            directoryStream.iterator().forEachRemaining(paths::add);
            files = paths.iterator();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 参照結果のファイルを1行づつ返却する。
     *
     * @param ctx 実行コンテキスト
     * @return ファイルデータを保持するオブジェクト
     */
    @Override
    public synchronized File read(ExecutionContext ctx) {
        if (hasNext(ctx)) {
            return files.next().toFile();
        }
        return null;
    }

    /**
     * 次のファイルが存在するかどうかを返却する。
     *
     * @param ctx 実行コンテキスト
     * @return 次に読み込むファイルがまだ残っている場合はtrue。
     */
    @Override
    public synchronized boolean hasNext(ExecutionContext ctx) {
        return files.hasNext();
    }

    /**
     * リソース解放処理。<br>
     * <br>
     * 読み込み対象のリソースを解放する。
     * @param ctx コンテキスト
     */
    @Override
    public void close(ExecutionContext ctx) {
        // nop
    }
}
