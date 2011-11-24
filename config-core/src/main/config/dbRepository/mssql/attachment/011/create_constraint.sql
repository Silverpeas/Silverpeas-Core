ALTER TABLE SB_Attachment_Attachment WITH NOCHECK ADD 
	 CONSTRAINT PK_Attachment_Attachment PRIMARY KEY  CLUSTERED 
	(
		attachmentId
	)   
;

ALTER TABLE SB_Attachment_AttachmentI18N ADD 
	 CONSTRAINT PK_Attachment_AttachmentI18N PRIMARY KEY
	(
		id
	)   
;