UPDATE ST_User SET sensitiveData = 0;

ALTER TABLE ST_User ALTER COLUMN sensitiveData BIT NOT NULL;