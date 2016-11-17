package com.nablarch.example.app.batch.action;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.nablarch.example.app.entity.FileCreateRequest;
import com.nablarch.example.app.entity.FileData;
import nablarch.common.dao.UniversalDao;
import nablarch.core.date.SystemTimeUtil;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.util.DateUtil;
import nablarch.core.util.FileUtil;
import nablarch.fw.DataReader;
import nablarch.test.core.db.DbAccessTestSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    private DbAccessTestSupport transaction = new DbAccessTestSupport(getClass());

    @Before
    public void setUp() throws Exception {
        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader("registration-pdf-file.xml")));
        transaction.beginTransactions();

        // FileCreateRequest初期化
        Date sysDate = SystemTimeUtil.getDate();
        FileCreateRequest insertFIle = new FileCreateRequest();
        for (int i = 0; i < TEST_DATA_ID.length; i++) {
            insertFIle.setFileId(TEST_DATA_ID[i]);
            UniversalDao.delete(insertFIle);

            insertFIle.setFileName(TEST_DATA_NAME[i]);
            insertFIle.setCreateTime(sysDate);
            insertFIle.setStatus("0");
            UniversalDao.insert(insertFIle);
        }

        // FileData初期化
        FileData fileData = new FileData();
        fileData.setFileDataId(TEST_DATA_ID[0]);
        UniversalDao.delete(fileData);
    }
    @After
    public void tearDown() {
        transaction.endTransactions();
    }

    @Test
    public void testCreateReader() {
        //テスト比較データ作成
        List<String> fileComparisonList = Arrays.asList(TEST_DATA_NAME);

        // 処理済みのため、対象として取得されないデータを作成
        FileCreateRequest dummy = new FileCreateRequest();
        dummy.setFileId("3");
        dummy.setFileName("dummy.pdf");
        dummy.setCreateTime(SystemTimeUtil.getDate());
        dummy.setStatus("1");
        UniversalDao.insert(dummy);

        DataReader<FileCreateRequest> reader = new RegistrationPdfFileAction().createReader(null);


        //リーダーのファイル名リスト作成
        List<String> fileList = new ArrayList<String>();
        while (reader.hasNext(null)) {
            fileList.add(reader.read(null).getFileName());
        }
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
    }

    /**
     * 成功時にステータスが適切に書き換わること。
     */
    @Test
    public void testSuccess() {
        //テスト対象クラス
        RegistrationPdfFileAction targetClass = new RegistrationPdfFileAction();

        //テスト対処クラスパラメータ作成
        FileCreateRequest inputData = UniversalDao.findById(FileCreateRequest.class, TEST_DATA_ID[0]);

        targetClass.transactionSuccess(inputData, null);

        //登録済みファイルのDBステータス確認
        FileCreateRequest result = UniversalDao.findById(FileCreateRequest.class, inputData.getFileId());
        assertThat(result.getStatus(), is("1"));
    }

    /**
     * 失敗時にステータスが適切に書き換わること。
     */
    @Test
    public void testFailure() {
        //テスト対象クラス
        RegistrationPdfFileAction targetClass = new RegistrationPdfFileAction();

        //テスト対処クラスパラメータ作成
        FileCreateRequest inputData = UniversalDao.findById(FileCreateRequest.class, TEST_DATA_ID[0]);

        targetClass.transactionFailure(inputData, null);

        //登録済みファイルのDBステータス確認
        FileCreateRequest result = UniversalDao.findById(FileCreateRequest.class, inputData.getFileId());
        assertThat(result.getStatus(), is("2"));
    }

}
