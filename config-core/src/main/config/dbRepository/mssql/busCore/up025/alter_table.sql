ALTER TABLE personalization DROP COLUMN onlineEditingStatus;
ALTER TABLE personalization ADD menuDisplay varchar(50) DEFAULT('DISABLE');