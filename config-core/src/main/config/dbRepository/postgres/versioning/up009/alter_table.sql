ALTER TABLE sb_version_document ALTER COLUMN documentName TYPE VARCHAR(255);
ALTER TABLE sb_version_document ALTER COLUMN documentName SET NOT NULL;

ALTER TABLE sb_version_version ALTER COLUMN versionLogicalName TYPE VARCHAR(255);
ALTER TABLE sb_version_version ALTER COLUMN versionLogicalName SET NOT NULL;