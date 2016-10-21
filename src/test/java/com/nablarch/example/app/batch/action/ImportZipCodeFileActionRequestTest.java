package com.nablarch.example.app.batch.action;

import nablarch.test.core.batch.BatchRequestTestSupport;
import org.junit.Test;

/**
 * 住所登録バッチのリクエスト単体テストクラス。
 */
public class ImportZipCodeFileActionRequestTest extends BatchRequestTestSupport {

    /** 正常系のテスト。 */
    @Test
    public void testNormalEnd() {
        execute();
    }

    /** 異常系のテスト。 */
    @Test
    public void testAbNormalEnd() {
        execute();
    }
}
