ALTER TABLE SB_Attachment_Attachment ADD (attachmentLogicalName_tmp VARCHAR2(255));
UPDATE SB_Attachment_Attachment SET attachmentLogicalName_tmp = attachmentLogicalName;
ALTER TABLE SB_Attachment_Attachment DROP COLUMN attachmentLogicalName;
ALTER TABLE SB_Attachment_Attachment RENAME COLUMN attachmentLogicalName_tmp TO attachmentLogicalName;
ALTER TABLE SB_Attachment_Attachment MODIFY attachmentLogicalName NOT NULL;


ALTER TABLE SB_Attachment_AttachmentI18N ADD (attachmentLogicalName_tmp VARCHAR2(255));
UPDATE SB_Attachment_AttachmentI18N SET attachmentLogicalName_tmp = attachmentLogicalName;
ALTER TABLE SB_Attachment_AttachmentI18N DROP COLUMN attachmentLogicalName;
ALTER TABLE SB_Attachment_AttachmentI18N RENAME COLUMN attachmentLogicalName_tmp TO attachmentLogicalName;
ALTER TABLE SB_Attachment_AttachmentI18N MODIFY attachmentLogicalName NOT NULL;

