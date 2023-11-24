ALTER TABLE ST_User ADD sensitiveData INT DEFAULT 0;

UPDATE ST_User SET sensitiveData = 0;

ALTER TABLE ST_User MODIFY sensitiveData NOT NULL;