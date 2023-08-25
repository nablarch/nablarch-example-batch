package com.nablarch.example.app.batch.util;

import nablarch.test.core.util.generator.CharacterGeneratorBase;

import static com.nablarch.example.app.batch.util.ExampleCharacterSet.*;


public class ExampleJapaneseCharacterGenerator extends CharacterGeneratorBase {

    /** 文字種と文字集合の組み合わせ */
    private static final String[][] TYPE_CHARS_PAIRS = {
            {"数字", NUMERIC},
            {"スペース", SPACE},
            {"半角カタカナ", HANKAKU_KANA_CHARS},
            {"地名漢字", CHIMEI_KANJI},
            {"制限文字", RESTRICTED_CHARS}
    };

    /**
     * コンストラクタ。
     */
    public ExampleJapaneseCharacterGenerator() {
        super(TYPE_CHARS_PAIRS);
    }
}
