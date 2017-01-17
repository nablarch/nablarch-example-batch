package com.nablarch.example.app.batch.reader;

import java.util.Iterator;
import java.util.UUID;

import nablarch.common.dao.DeferredEntityList;
import nablarch.common.dao.UniversalDao;
import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.transaction.SimpleDbTransactionExecutor;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.message.ApplicationException;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;

import com.nablarch.example.app.entity.FileCreateRequest;

/**
 * ファイル生成リクエストリーダクラス。
 * @author Nabu Rakutaro
 *
 */
@Published
public class FileCreateRequestReader implements DataReader<FileCreateRequest> {
    
    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(FileCreateRequestReader.class);
    
    /** 参照結果レコードのイテレータ */
    private final Iterator<FileCreateRequest> records;

    /** ファイル生成リクエストのリスト */
    private final DeferredEntityList<FileCreateRequest> instance;

    /** プロセスID(UUIDより生成する) */
    private static final String PROCESS_ID = UUID.randomUUID()
                                                 .toString();

    /**
     * コンストラクタ。
     *
     * @throws ApplicationException 処理対象のファイル生成リクエストが存在しない場合
     */
    @Published
    public FileCreateRequestReader() {
        markUnprocessedData();
        instance = (DeferredEntityList<FileCreateRequest>)
                UniversalDao.defer()
                            .findAllBySqlFile(FileCreateRequest.class, "GET_MISHORI_FILE_INFO",
                                    new Object[] {PROCESS_ID});
        // Iteratorとして保持
        records = instance.iterator();
        if (!records.hasNext()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.logDebug("登録対象ファイルなし。");
            }
        }
    }

    /**
     * 未処理のデータをマーク(悲観ロック)し、他のプロセスで処理されないようにする。
     */
    private static void markUnprocessedData() {
        final SimpleDbTransactionManager myTran = SystemRepository.get("myTran");
        new SimpleDbTransactionExecutor<Void>(myTran) {
            @Override
            public Void execute(final AppDbConnection connection) {
                final SqlPStatement statement = connection.prepareStatementBySqlId(
                        FileCreateRequest.class.getName() + "#MARK_UNPROCESSED_DATA");
                statement.setString(1, PROCESS_ID);
                statement.executeUpdate();
                return null;
            }
        }.doTransaction();
    }

    @Override
    public synchronized FileCreateRequest read(ExecutionContext ctx) {
        return records.next();
    }

    @Override
    public synchronized boolean hasNext(ExecutionContext ctx) {
        return records.hasNext();
    }

    /**
     * リソース解放処理。<br>
     * <br>
     * 読み込み対象のリソースを解放する。
     * @param ctx コンテキスト
     */
    @Override
    public void close(ExecutionContext ctx) {
        if (instance != null) {
            instance.close();
        }
    }
}
