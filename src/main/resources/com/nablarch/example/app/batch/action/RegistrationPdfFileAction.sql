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
