package com.nablarch.example.app.batch.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Random;

import nablarch.common.dao.DeferredEntityList;
import nablarch.common.dao.UniversalDao;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.Result.Success;
import nablarch.fw.action.BatchAction;

import com.nablarch.example.app.batch.reader.FileCreateRequestReader;
import com.nablarch.example.app.entity.FileCreateRequest;
import com.nablarch.example.app.entity.FileData;

/**
 * PDFファイル登録クラス。
 * @author Nabu Rakutaro
 *
 */
@Published
public class RegistrationPdfFileAction extends BatchAction<FileCreateRequest> {

    /** 設定キー：入力ファイルパス */
    private static final String FILE_PATH_KEY_INPUT = "RegistrationPdfFile.batch.input";
    /** 設定キー：作業ファイルパス */
    private static final String FILE_PATH_KEY_WORK = "RegistrationPdfFile.batch.work";

    /** DeferredEntityListリソース解放の為内部保持 */
    private FileCreateRequestReader reader;

    @Override
    public DataReader<FileCreateRequest> createReader(ExecutionContext context) {

        DeferredEntityList<FileCreateRequest> entityList
            = (DeferredEntityList<FileCreateRequest>) UniversalDao.defer().findAll(FileCreateRequest.class);

        reader = new FileCreateRequestReader(entityList);
        return reader;
    }

    @Override
    public Result handle(FileCreateRequest inputData, ExecutionContext context) {

        // 対象ファイル名取得
        String fileName = inputData.getFileName();
        // 入力Dirから作業Dirへファイルを移動
        File inputFile = new File(SystemRepository.getString(FILE_PATH_KEY_INPUT), fileName);
        File workFile = new File(SystemRepository.getString(FILE_PATH_KEY_WORK), fileName);
        FileUtil.move(inputFile, workFile);

        // 処理が単純で、応答時間が非常に短いので実務処理に近づけるためSleepさせる
        try {
            Thread.sleep(new Random().nextInt(20000)); // CHECKSTYLE IGNORE THIS LINE
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // ファイルデータ登録
        Date sysDate = SystemTimeUtil.getDate();
        FileData fileData = new FileData();
        fileData.setFileDataId(inputData.getFileId());
        fileData.setFileName(fileName);
        fileData.setFileSize(workFile.length());
        fileData.setFileData(readFileToByte(workFile));
        fileData.setCreateTime(sysDate);
        UniversalDao.insert(fileData);

        // 登録済みのファイルを削除
        FileUtil.deleteFile(workFile);

        // ファイルリクエスト削除
        UniversalDao.delete(inputData);

        return new Success();
    }

    @Override
    public void terminate(Result result, ExecutionContext context) {
        reader.close(context);
    }

    /**
     * ファイルを読み込み、その中身をバイト配列で取得する。
     *
     * @param file 対象ファイル
     * @return 読み込んだバイト配列
     * @throws RuntimeException ファイルが見つからない、アクセスできないときなど
     */
    protected byte[] readFileToByte(File file) {
        Path path = Paths.get(file.getAbsolutePath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
