ALTER TABLE uniqueid ADD (maxId_tmp NUMBER(19,0));
UPDATE uniqueid SET maxId_tmp = maxId;
ALTER TABLE uniqueid DROP COLUMN maxId;
ALTER TABLE uniqueid RENAME COLUMN maxId_tmp TO maxId;
ALTER TABLE uniqueid MODIFY maxId NOT NULL;