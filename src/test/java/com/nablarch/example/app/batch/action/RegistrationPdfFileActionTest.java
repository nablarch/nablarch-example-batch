package com.nablarch.example.app.batch.action;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nablarch.common.dao.UniversalDao;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.util.DateUtil;
import nablarch.core.util.FileUtil;
import nablarch.fw.DataReader;
import nablarch.test.core.db.DbAccessTestSupport;

import org.junit.Before;
import org.junit.Test;

import com.nablarch.example.app.entity.FileCreateRequest;
import com.nablarch.example.app.entity.FileData;
/**
 * 常駐起動バッチテストクラス。
 * @author Nabu Rakutaro
 *
 */
public class RegistrationPdfFileActionTest {

    /** 設定キー：入力ファイルパス */
    private static final String FILE_PATH_KEY_INPUT = "RegistrationPdfFile.batch.input";
    /** 設定キー：作業ファイルパス */
    private static final String FILE_PATH_KEY_WORK = "RegistrationPdfFile.batch.work";
    /** 設定キー：テストデータファイルパス */
    private static final String FILE_PATH_KEY_TEST = "RegistrationPdfFile.batch.test";

    /** テストデータ */
    private static final String[] TEST_DATA_ID = { "1", "2" };
    private static final String[] TEST_DATA_NAME = { "test1.pdf", "test2.pdf" };

    @Before
    public void setUp() throws Exception {
        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader("registration-pdf-file.xml")));
        new DbAccessTestSupport(getClass()).beginTransactions();

        // FileCreateRequest初期化
        Date sysDate = SystemTimeUtil.getDate();
        FileCreateRequest insertFIle = new FileCreateRequest();
        for (int i = 0; i < TEST_DATA_ID.length; i++) {
            insertFIle.setFileId(TEST_DATA_ID[i]);
            UniversalDao.delete(insertFIle);
            insertFIle.setFileName(TEST_DATA_NAME[i]);
            insertFIle.setCreateTime(sysDate);
            UniversalDao.insert(insertFIle);
        }

        // FileData初期化
        FileData fileData = new FileData();
        fileData.setFileDataId(TEST_DATA_ID[0]);
        UniversalDao.delete(fileData);
    }

    @Test
    public void testCreateReader() {
        //テスト比較データ作成
        List<String> fileComparisonList = Arrays.asList(TEST_DATA_NAME);

        DataReader<FileCreateRequest> reader = new RegistrationPdfFileAction().createReader(null);


        //リーダーのファイル名リスト作成
        List<String> fileList = new ArrayList<String>();
        while (reader.hasNext(null)) {
            fileList.add(reader.read(null).getFileName());
        }
        new SimpleDbTransactionManager().endTransaction();
        //リーダーのファイル名取得確認
        assertThat(fileList, is(fileComparisonList));
    }

    @Test
    public void testHandleFileCreateRequest() {
        FileUtil.copy(new File(SystemRepository.getString(FILE_PATH_KEY_TEST), TEST_DATA_NAME[0]), new File(SystemRepository.getString(FILE_PATH_KEY_INPUT), TEST_DATA_NAME[0]));
        //テスト対象クラス
        RegistrationPdfFileAction targetClass = new RegistrationPdfFileAction();

        //テスト比較データ作成
        File targetFile = new File(SystemRepository.getString(FILE_PATH_KEY_INPUT), TEST_DATA_NAME[0]);
        Long targetLength = targetFile.length();
        byte[] comparisonData = targetClass.readFileToByte(targetFile);

        //テスト対処クラスパラメータ作成
        FileCreateRequest inputData = new FileCreateRequest();
        inputData.setFileId(TEST_DATA_ID[0]);
        inputData.setFileName(TEST_DATA_NAME[0]);

        targetClass.handle(inputData, null);

        // DB登録内容確認
        FileData selectPdfDate = UniversalDao.findById(FileData.class, TEST_DATA_ID[0]);
        assertThat(selectPdfDate.getFileDataId(), is(TEST_DATA_ID[0]));
        assertThat(selectPdfDate.getFileName(), is(TEST_DATA_NAME[0]));
        assertThat(selectPdfDate.getFileSize(), is(targetLength));
        assertThat(selectPdfDate.getFileData(), is(comparisonData));
        assertThat(DateUtil.formatDate(selectPdfDate.getCreateTime(), "yyyyMMdd"),
                is(DateUtil.formatDate(SystemTimeUtil.getDate(), "yyyyMMdd")));
        //登録済みファイルの削除確認
        assertFalse(targetFile.exists());
        assertFalse(new File(SystemRepository.getString(FILE_PATH_KEY_WORK), TEST_DATA_NAME[0]).exists());
        //登録済みファイルのDB削除確認
        assertEquals(UniversalDao.delete(inputData), 0);
        new SimpleDbTransactionManager().endTransaction();
    }
}
