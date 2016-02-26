ALTER TABLE sb_attachment_attachment ALTER COLUMN attachmentLogicalName TYPE VARCHAR(255);
ALTER TABLE sb_attachment_attachment ALTER COLUMN attachmentLogicalName SET NOT NULL;
ALTER TABLE sb_attachment_attachmentI18N ALTER COLUMN attachmentLogicalName TYPE VARCHAR(255);
ALTER TABLE sb_attachment_attachmentI18N ALTER COLUMN attachmentLogicalName SET NOT NULL;