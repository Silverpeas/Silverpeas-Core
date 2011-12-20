ALTER TABLE SB_Version_Document ADD (documentName_tmp VARCHAR2(255));
UPDATE SB_Version_Document SET documentName_tmp = documentName;
ALTER TABLE SB_Version_Document DROP COLUMN documentName;
ALTER TABLE SB_Version_Document RENAME COLUMN documentName_tmp TO documentName;
ALTER TABLE SB_Version_Document MODIFY documentName NOT NULL;


ALTER TABLE SB_Version_Version ADD (versionLogicalName_tmp VARCHAR2(255));
UPDATE SB_Version_Version SET versionLogicalName_tmp = versionLogicalName;
ALTER TABLE SB_Version_Version DROP COLUMN versionLogicalName;
ALTER TABLE SB_Version_Version RENAME COLUMN versionLogicalName_tmp TO versionLogicalName;
ALTER TABLE SB_Version_Version MODIFY versionLogicalName NOT NULL;

