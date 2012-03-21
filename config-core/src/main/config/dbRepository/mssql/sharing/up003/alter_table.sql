ALTER TABLE sb_filesharing_ticket ADD fileId_temp BIGINT;
UPDATE sb_filesharing_ticket SET fileId_temp = fileId;
ALTER TABLE sb_filesharing_ticket DROP COLUMN fileId;
ALTER TABLE sb_filesharing_ticket ADD shared_object BIGINT;
UPDATE sb_filesharing_ticket SET shared_object = fileId_temp;
ALTER TABLE sb_filesharing_ticket ALTER COLUMN shared_object BIGINT NOT NULL;
ALTER TABLE sb_filesharing_ticket DROP COLUMN fileId_temp;

ALTER TABLE sb_filesharing_ticket ADD shared_object_type VARCHAR(255);
UPDATE sb_filesharing_ticket SET shared_object_type = 'Attachment' WHERE versioning = '0';
UPDATE sb_filesharing_ticket SET shared_object_type = 'Versionned' WHERE versioning = '1';
ALTER TABLE sb_filesharing_ticket ALTER COLUMN shared_object_type VARCHAR(255) NOT NULL;
ALTER TABLE sb_filesharing_ticket DROP COLUMN versioning;

ALTER TABLE SB_fileSharing_ticket DROP CONSTRAINT PK_SB_fileSharing_ticket;
DROP INDEX IND_Ticket ON SB_fileSharing_ticket;
ALTER TABLE sb_filesharing_ticket ALTER COLUMN keyfile VARCHAR(255) NOT NULL;
CREATE INDEX IND_Ticket ON SB_fileSharing_ticket (keyFile);
ALTER TABLE SB_fileSharing_ticket ADD CONSTRAINT PK_SB_fileSharing_ticket PRIMARY KEY (keyFile);

ALTER TABLE sb_filesharing_ticket ADD creationDate_temp BIGINT;
UPDATE sb_filesharing_ticket SET creationDate_temp = creationDate;
ALTER TABLE sb_filesharing_ticket DROP COLUMN creationDate;
ALTER TABLE sb_filesharing_ticket ADD creationDate BIGINT ;
UPDATE sb_filesharing_ticket SET creationDate = creationDate_temp;
ALTER TABLE sb_filesharing_ticket ALTER COLUMN creationDate BIGINT NOT NULL;
ALTER TABLE sb_filesharing_ticket DROP COLUMN creationDate_temp;

ALTER TABLE sb_filesharing_ticket ADD updateDate_temp BIGINT;
UPDATE sb_filesharing_ticket SET updateDate_temp = updateDate;
ALTER TABLE sb_filesharing_ticket DROP COLUMN updateDate;
ALTER TABLE sb_filesharing_ticket ADD updateDate BIGINT ;
UPDATE sb_filesharing_ticket SET updateDate = updateDate_temp;
ALTER TABLE sb_filesharing_ticket DROP COLUMN updateDate_temp;

ALTER TABLE sb_filesharing_ticket ADD endDate_temp BIGINT;
UPDATE sb_filesharing_ticket SET endDate_temp = endDate;
ALTER TABLE sb_filesharing_ticket DROP COLUMN endDate;
ALTER TABLE sb_filesharing_ticket ADD endDate BIGINT ;
UPDATE sb_filesharing_ticket SET endDate = endDate_temp;
ALTER TABLE sb_filesharing_ticket DROP COLUMN endDate_temp;


ALTER TABLE sb_filesharing_history ADD id_temp BIGINT;
UPDATE sb_filesharing_history SET id_temp = id;
ALTER TABLE sb_filesharing_history DROP CONSTRAINT PK_SB_fileSharing_history ;
DROP INDEX IND_History ON sb_filesharing_history;
ALTER TABLE sb_filesharing_history DROP COLUMN id;
ALTER TABLE sb_filesharing_history ADD id BIGINT;
UPDATE sb_filesharing_history SET id = id_temp;
ALTER TABLE sb_filesharing_history ALTER COLUMN id BIGINT NOT NULL;
ALTER TABLE sb_filesharing_history DROP COLUMN id_temp;

ALTER TABLE sb_filesharing_history ALTER COLUMN keyfile VARCHAR(255) NOT NULL;

ALTER TABLE sb_filesharing_history ADD downloadDate_temp BIGINT;
UPDATE sb_filesharing_history SET downloadDate_temp = downloadDate;
ALTER TABLE sb_filesharing_history DROP COLUMN downloadDate;
ALTER TABLE sb_filesharing_history ADD downloadDate BIGINT ;
UPDATE sb_filesharing_history SET downloadDate = downloadDate_temp;
ALTER TABLE sb_filesharing_history ALTER COLUMN downloadDate BIGINT NOT NULL;
ALTER TABLE sb_filesharing_history DROP COLUMN downloadDate_temp;

UPDATE sb_filesharing_ticket SET nbaccess = 0 WHERE nbaccess IS NULL;