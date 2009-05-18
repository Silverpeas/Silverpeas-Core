alter table SB_Attachment_Attachment
alter column attachmentPhysicalName varchar (500) NOT NULL
;
alter table SB_Attachment_Attachment
alter column attachmentLogicalName varchar (100) NOT NULL
;
alter table SB_Attachment_Attachment
alter column attachmentType varchar (100) NULL
;
alter table SB_Attachment_Attachment
alter column attachmentSize varchar (100) NULL
;
alter table SB_Attachment_Attachment
alter column attachmentContext varchar (500) NULL
;
alter table SB_Attachment_Attachment
alter column attachmentForeignkey varchar (100) NOT NULL
;
ALTER TABLE SB_Attachment_Attachment
ADD attachmentAuthor varchar (100) NULL
;
