package com.nablarch.example.app.batch.action;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nablarch.core.date.SystemTimeUtil;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.util.DateUtil;
import nablarch.core.util.FileUtil;
import nablarch.fw.DataReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 都度起動バッチテストクラス。
 * @author Nabu Rakutaro
 *
 */
class FileDeleteActionTest {

    /** 設定キー：作業ファイルパス */
    static final String FILE_PATH_KEY_WORK = "RegistrationPdfFile.batch.work";
    /** 設定キー：テストデータファイルパス */
    static final String FILE_PATH_KEY_TEST = "RegistrationPdfFile.batch.test";

    /** テストデータ */
    static final String[] TEST_DATA_NAME = { "test1.pdf", "test2.pdf" };

    @BeforeEach
    void setUp() throws Exception {
        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader("file-delete.xml")));
    }

    @Test
    void testCreateReader() {
        FileUtil.copy(new File(SystemRepository.getString(FILE_PATH_KEY_TEST), TEST_DATA_NAME[0]), new File(SystemRepository.getString(FILE_PATH_KEY_WORK), TEST_DATA_NAME[0]));
        FileUtil.copy(new File(SystemRepository.getString(FILE_PATH_KEY_TEST), TEST_DATA_NAME[1]), new File(SystemRepository.getString(FILE_PATH_KEY_WORK), TEST_DATA_NAME[1]));
        List<File> fileComparisonList = new ArrayList<File>();
        fileComparisonList.add(new File(SystemRepository.getString(FILE_PATH_KEY_WORK), TEST_DATA_NAME[0]));
        fileComparisonList.add(new File(SystemRepository.getString(FILE_PATH_KEY_WORK), TEST_DATA_NAME[1]));

        DataReader<File> reader = new FileDeleteAction().createReader(null);

        //リーダーのファイル名リスト作成
        List<File> fileList = new ArrayList<File>();
        while (reader.hasNext(null)) {
            fileList.add(reader.read(null));
        }
        //リーダーのファイル名取得確認
        assertThat(fileList, is(fileComparisonList));
    }

    @Test
    void testHandleFile() {
        File target = new File(SystemRepository.getString(FILE_PATH_KEY_WORK), TEST_DATA_NAME[0]);
        FileUtil.copy(new File(SystemRepository.getString(FILE_PATH_KEY_TEST), TEST_DATA_NAME[0]), target);
        target.setLastModified(DateUtil.getDate(DateUtil.addDay(SystemTimeUtil.getDateString(), -2)).getTime());

        File inputData = new File(SystemRepository.getString(FILE_PATH_KEY_WORK), TEST_DATA_NAME[0]);
        new FileDeleteAction().handle(inputData, null);

        //ファイル削除確認
        assertFalse(inputData.exists());
    }

}
