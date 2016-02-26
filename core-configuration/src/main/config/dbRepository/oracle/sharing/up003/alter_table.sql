ALTER TABLE sb_filesharing_ticket ADD shared_object number(19,0);
UPDATE sb_filesharing_ticket SET shared_object = to_number(fileId);
ALTER TABLE sb_filesharing_ticket DROP COLUMN fileId;
ALTER TABLE sb_filesharing_ticket MODIFY shared_object NOT NULL;

ALTER TABLE sb_filesharing_ticket DROP CONSTRAINT PK_SB_fileSharing_ticket;
ALTER TABLE sb_filesharing_ticket ADD keyFile_temp VARCHAR2(255);
UPDATE sb_filesharing_ticket SET keyFile_temp = keyFile;
ALTER TABLE sb_filesharing_ticket DROP COLUMN keyFile;
ALTER TABLE sb_filesharing_ticket ADD keyFile VARCHAR2(255);
UPDATE sb_filesharing_ticket SET keyFile = keyFile_temp;
ALTER TABLE sb_filesharing_ticket DROP COLUMN keyFile_temp;
ALTER TABLE sb_filesharing_ticket MODIFY keyFile NOT NULL;
ALTER TABLE SB_fileSharing_ticket ADD CONSTRAINT PK_SB_fileSharing_ticket PRIMARY KEY (keyFile);

ALTER TABLE sb_filesharing_ticket ADD shared_object_type VARCHAR2(255);
UPDATE sb_filesharing_ticket SET shared_object_type = 'Attachment' WHERE versioning = '0';
UPDATE sb_filesharing_ticket SET shared_object_type = 'Versionned' WHERE versioning = '1';
ALTER TABLE sb_filesharing_ticket DROP COLUMN versioning;
ALTER TABLE sb_filesharing_ticket MODIFY shared_object_type NOT NULL;


ALTER TABLE sb_filesharing_ticket ADD creationDate_temp NUMBER(19,0);
UPDATE sb_filesharing_ticket SET creationDate_temp  = TO_NUMBER(creationDate);
ALTER TABLE sb_filesharing_ticket DROP COLUMN creationDate;
ALTER TABLE sb_filesharing_ticket ADD creationDate NUMBER(19,0);
UPDATE sb_filesharing_ticket SET creationDate = creationDate_temp;
ALTER TABLE sb_filesharing_ticket DROP COLUMN creationDate_temp;
ALTER TABLE sb_filesharing_ticket MODIFY creationDate NOT NULL;


ALTER TABLE sb_filesharing_ticket ADD updateDate_temp NUMBER(19,0);
UPDATE sb_filesharing_ticket SET updateDate_temp = TO_NUMBER(updateDate);
ALTER TABLE sb_filesharing_ticket DROP COLUMN updateDate;
ALTER TABLE sb_filesharing_ticket ADD updateDate NUMBER(19,0);
UPDATE sb_filesharing_ticket SET updateDate = updateDate_temp;
ALTER TABLE sb_filesharing_ticket DROP COLUMN updateDate_temp;

ALTER TABLE sb_filesharing_ticket ADD endDate_temp NUMBER(19,0);
UPDATE sb_filesharing_ticket SET endDate_temp = TO_NUMBER(endDate);
ALTER TABLE sb_filesharing_ticket DROP COLUMN endDate;
ALTER TABLE sb_filesharing_ticket ADD endDate NUMBER(19,0);
UPDATE sb_filesharing_ticket SET endDate = endDate_temp;
ALTER TABLE sb_filesharing_ticket DROP COLUMN endDate_temp;


ALTER TABLE sb_filesharing_history DROP CONSTRAINT PK_SB_fileSharing_history;
ALTER TABLE sb_filesharing_history ADD id_temp NUMBER(19, 0);
UPDATE sb_filesharing_history SET id_temp = id;
ALTER TABLE sb_filesharing_history DROP COLUMN id;
ALTER TABLE sb_filesharing_history ADD id NUMBER(19, 0);
UPDATE sb_filesharing_history SET id = id_temp;
ALTER TABLE sb_filesharing_history DROP COLUMN id_temp;
ALTER TABLE sb_filesharing_history MODIFY id NOT NULL;
ALTER TABLE SB_fileSharing_history ADD CONSTRAINT PK_SB_fileSharing_history PRIMARY KEY (id);

ALTER TABLE sb_filesharing_history ADD downloadDate_temp NUMBER(19,0);
UPDATE sb_filesharing_history SET downloadDate_temp = TO_NUMBER(downloadDate);
ALTER TABLE sb_filesharing_history DROP COLUMN downloadDate;
ALTER TABLE sb_filesharing_history ADD downloadDate NUMBER(19,0);
UPDATE sb_filesharing_history SET downloadDate = downloadDate_temp;
ALTER TABLE sb_filesharing_history DROP COLUMN downloadDate_temp;

ALTER TABLE sb_filesharing_history ADD keyFile_temp VARCHAR2(255);
UPDATE sb_filesharing_history SET keyFile_temp = keyFile;
ALTER TABLE sb_filesharing_history DROP COLUMN keyFile;
ALTER TABLE sb_filesharing_history ADD keyFile VARCHAR2(255);
UPDATE sb_filesharing_history SET keyFile = keyFile_temp;
ALTER TABLE sb_filesharing_history DROP COLUMN keyFile_temp;
ALTER TABLE sb_filesharing_history MODIFY keyFile NOT NULL;

UPDATE sb_filesharing_ticket SET nbaccess = 0 WHERE nbaccess IS NULL;