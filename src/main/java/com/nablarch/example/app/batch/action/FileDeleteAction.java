package com.nablarch.example.app.batch.action;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;

import nablarch.core.date.SystemTimeUtil;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.DateUtil;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.Result.Success;
import nablarch.fw.action.BatchAction;

import com.nablarch.example.app.batch.reader.DeleteTargetFileReader;

/**
 * ファイル削除クラス。
 * @author Nabu Rakutaro
 *
 */
@Published
public class FileDeleteAction extends BatchAction<File> {

    /** 設定キー：ファイルパス */
    private static final String FILE_PATH_KEY = "RegistrationPdfFile.batch.work";

    @Override
    public DataReader<File> createReader(ExecutionContext context) {
        return new DeleteTargetFileReader(Paths.get(SystemRepository.getString(FILE_PATH_KEY)), "*pdf");
    }

    @Override
    public Result handle(File inputData, ExecutionContext context) {

        String dateFileUpdateTime = DateUtil.formatDate(new Date(inputData.lastModified()), "yyyyMMdd");
        String yesterday = DateUtil.addDay(SystemTimeUtil.getDateString(), -1);
        // 更新日時が前日の日付より前だったらファイル削除
        if (dateFileUpdateTime.compareTo(yesterday) < 0) {
            FileUtil.deleteFile(inputData);
        }

        return new Success();
    }

}
