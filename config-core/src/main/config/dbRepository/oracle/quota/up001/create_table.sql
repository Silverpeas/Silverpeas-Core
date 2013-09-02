ALTER TABLE st_quota
ADD (minCount_tmp number(19,0))
;
ALTER TABLE st_quota
ADD (maxCount_tmp number(19,0))
;
ALTER TABLE st_quota
ADD (currentCount_tmp number(19,0))
;

UPDATE st_quota
SET minCount_tmp = minCount , maxCount_tmp = maxCount, currentCount_tmp = currentCount
;

ALTER TABLE st_quota
DROP COLUMN minCount
;
ALTER TABLE st_quota
DROP COLUMN maxCount
;
ALTER TABLE st_quota
DROP COLUMN currentCount
;

ALTER TABLE st_quota
RENAME COLUMN minCount_tmp TO minCount
;
ALTER TABLE st_quota
RENAME COLUMN maxCount_tmp TO maxCount
;
ALTER TABLE st_quota
RENAME COLUMN currentCount_tmp TO currentCount
;

ALTER TABLE st_quota
MODIFY (minCount NOT NULL)
;
ALTER TABLE st_quota
MODIFY (maxCount NOT NULL)
;
ALTER TABLE st_quota
MODIFY (currentCount NOT NULL)
;