-------------------------------------------------------------------------------
-- 常駐バッチ未処理ファイル情報の取得
-------------------------------------------------------------------------------
GET_MISHORI_FILE_INFO=
SELECT
    FILE_ID,
    FILE_NAME,
    CREATE_TIME,
    STATUS,
    PROCESS_ID
FROM
    FILE_CREATE_REQUEST
WHERE
    STATUS = '0'
    AND PROCESS_ID = ?

-------------------------------------------------------------------------------
-- 常駐バッチ未処理ファイル情報の取得
-------------------------------------------------------------------------------
MARK_UNPROCESSED_DATA =
UPDATE FILE_CREATE_REQUEST
SET PROCESS_ID = ?
WHERE STATUS = '0'
 AND PROCESS_ID IS NULL 
