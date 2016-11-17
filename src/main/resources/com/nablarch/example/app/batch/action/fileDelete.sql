-------------------------------------------------------------------------------
-- 常駐バッチ未処理ファイル情報の取得
-------------------------------------------------------------------------------
GET_MISHORI_FILE_INFO=
SELECT
    FILE_ID,
    FILE_NAME,
    CREATE_TIME,
    STATUS
FROM
    FILE_CREATE_REQUEST
WHERE
    STATUS = '0'

-------------------------------------------------------------------------------
-- 常駐バッチ処理済ファイル情報の取得
-------------------------------------------------------------------------------
GET_SHORIZUMI_FILE_INFO=
SELECT
    FILE_ID,
    FILE_NAME,
    SHORIZUMI_FLG,
    CREATE_TIME,
    UPDATE_TIME,
    DELETE_FLG
FROM
    FILE_CREATE_REQUEST
WHERE
    SHORIZUMI_FLG = :shorizumiFlg
AND
    DELETE_FLG = :deleteFlg
