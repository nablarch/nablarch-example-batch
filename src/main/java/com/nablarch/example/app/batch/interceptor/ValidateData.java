package com.nablarch.example.app.batch.interceptor;

import nablarch.core.beans.BeanUtil;
import nablarch.core.beans.BeansException;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ee.ValidatorUtil;
import nablarch.fw.ExecutionContext;

import nablarch.fw.Interceptor;
import nablarch.fw.Result;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * ハンドラの実行をインターセプトし、ハンドラに渡されるデータレコードを任意のクラスでBean Validationするインターセプタ。
 * <p>
 * バリデーションエラーが発生した場合はWARNレベルのログを出力し、インターセプトされたハンドラの処理は実行しない。
 * バリデーションエラーが発生していない場合は、入力データをインターセプトされたハンドラに引き渡して実行する。
 * <p>
 * エラー行の行番号をロギングしたい場合は、対象のBeanに「lineNumber」プロパティを定義する。
 * このプロパティがない場合、行番号は "null" と出力される。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(ValidateData.Impl.class)
@Published
public @interface ValidateData {
    /**
     * {@link ValidateData}インターセプタの実装。
     */
    class Impl extends Interceptor.Impl<Object, Result, ValidateData> {

        /** ロガー */
        private final Logger log = LoggerManager.get(ValidateData.class);

        /**
         * データレコードのBean Validationを実行する。
         * @param inputData データレコード
         * @param context コンテキスト
         * @return 結果
         */
        public Result handle(Object inputData, ExecutionContext context) {

            // BeanValidationを実行
            final Validator validator = ValidatorUtil.getValidator();
            final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(inputData);

            boolean errorFlg = false;

            for (ConstraintViolation<Object> violation : constraintViolations) {
                errorFlg = true;

                // 行番号プロパティが定義されているBeanのみ行番号を設定
                Long lineNumber = null;
                try {
                    lineNumber = (Long) BeanUtil.getProperty(inputData, "lineNumber");
                } catch (BeansException e) { //CHECKSTYLE IGNORE THIS LINE
                    // NOP
                }

                // バリデーションエラーの内容をロギングする
                Message message = MessageUtil.createMessage(MessageLevel.WARN,
                        "invalid_data_record", violation.getPropertyPath(),
                        violation.getMessage(), lineNumber);

                log.logWarn(message.formatMessage());
            }
            if (errorFlg) {
                return null;
            } else {
                return getOriginalHandler().handle(inputData, context);
            }

        }
    }
}
