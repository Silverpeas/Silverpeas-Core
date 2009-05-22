ALTER TABLE SB_Contact_Info ADD (modelId_tmp VARCHAR2(100));
UPDATE SB_Contact_Info SET modelId_tmp = modelId;
ALTER TABLE SB_Contact_Info DROP COLUMN modelId;
ALTER TABLE SB_Contact_Info RENAME COLUMN modelId_tmp TO modelId;
ALTER TABLE SB_Contact_Info MODIFY modelId NOT NULL;
