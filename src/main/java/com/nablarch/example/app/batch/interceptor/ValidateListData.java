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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ハンドラの実行をインターセプトし、ハンドラに渡されるデータレコードを任意のクラスでBean Validationするインターセプタ。
 * <p>
 * バリデーションエラーが発生した場合はWARNレベルのログを出力し、インターセプトされたハンドラの処理は実行しない。
 * バリデーションエラーが発生していない場合は、入力データをインターセプトされたハンドラに引き渡して実行する。
 * <p>
 * エラー行の行番号をロギングしたい場合は、対象のBeanに「lineNumber」プロパティを定義する。
 * このプロパティがない場合、行番号は "null" と出力される。
 *
 * @author Nabu Rakutaro
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(ValidateListData.ValidateListDataImpl.class)
@Published
public @interface ValidateListData {

    /**
     * {@link ValidateListData}インターセプタの実装。
     */
    @SuppressWarnings("PublicInnerClass")
    class ValidateListDataImpl extends Interceptor.Impl<List<Object>, Result, ValidateListData> {

        /** ロガー */
        private static final Logger LOGGER = LoggerManager.get(ValidateListData.class);

        /**
         * データレコードのBean Validationを実行する。
         *
         * @param objects データレコード
         * @param context コンテキスト
         * @return 結果
         */
        @Override
        public Result handle(List<Object> objects, ExecutionContext context) {

            List<Object> nextData = new ArrayList<>();

            for (Object data : objects) {

                // BeanValidationを実行
                final Validator validator = ValidatorUtil.getValidator();
                final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(data);

                if (constraintViolations.isEmpty()) {
                    // バリデーションエラーがない場合は後続の処理を呼び出し
                    nextData.add(data);
                } else {
                    constraintViolations.stream()
                            .map(violation -> {
                                // 行番号プロパティが定義されているBeanのみ行番号を設定
                                Long lineNumber = null;
                                try {
                                    lineNumber = (Long) BeanUtil.getProperty(data, "lineNumber");
                                } catch (BeansException e) { //CHECKSTYLE IGNORE THIS LINE
                                    // NOP
                                }

                                // バリデーションエラーの内容をロギングする
                                Message message = MessageUtil.createMessage(
                                        MessageLevel.WARN,
                                        "invalid_data_record",
                                        violation.getPropertyPath(),
                                        violation.getMessage(),
                                        lineNumber);
                                return message.formatMessage();
                            })
                            .forEach(LOGGER::logWarn);
                }
            }

            if (nextData.isEmpty()) {
                return null;
            } else {
                return getOriginalHandler().handle(nextData, context);
            }
        }
    }
}
