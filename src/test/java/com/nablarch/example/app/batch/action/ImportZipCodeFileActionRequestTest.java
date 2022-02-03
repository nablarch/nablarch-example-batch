package com.nablarch.example.app.batch.action;

import nablarch.test.core.batch.BatchRequestTestSupport;
import nablarch.test.junit5.extension.batch.BatchRequestTest;
import org.junit.jupiter.api.Test;

/**
 * 住所登録バッチのリクエスト単体テストクラス。
 */
@BatchRequestTest
class ImportZipCodeFileActionRequestTest {

    BatchRequestTestSupport support;

    /** 正常系のテスト。 */
    @Test
    void testNormalEnd() {
        support.execute(support.testName.getMethodName());
    }

    /** 異常系のテスト。 */
    @Test
    void testAbNormalEnd() {
        support.execute(support.testName.getMethodName());
    }
}
