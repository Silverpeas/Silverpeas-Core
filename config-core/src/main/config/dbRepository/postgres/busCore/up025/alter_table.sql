ALTER TABLE personalization DROP COLUMN onlineEditingStatus;
ALTER TABLE personalization ADD COLUMN  menuDisplay varchar(50) DEFAULT 'DISABLE';
