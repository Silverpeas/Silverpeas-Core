ALTER TABLE st_quota
ALTER COLUMN minCount bigint NOT NULL
;
ALTER TABLE st_quota
ALTER COLUMN maxCount bigint NOT NULL
;
ALTER TABLE st_quota
ALTER COLUMN currentCount bigint NOT NULL
;