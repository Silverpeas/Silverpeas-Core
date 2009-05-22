ALTER TABLE SB_Contact_Contact WITH NOCHECK ADD 
	 CONSTRAINT PK_Contact_Contact PRIMARY KEY CLUSTERED 
	(
		contactId
	)   
;

ALTER TABLE SB_Contact_contactfather WITH NOCHECK ADD 
	 CONSTRAINT PK_Contact_contactfather PRIMARY KEY  CLUSTERED 
	(
		contactId,
		nodeId
	)   
;

ALTER TABLE SB_Contact_Info WITH NOCHECK ADD 
	 CONSTRAINT PK_Contact_Info PRIMARY KEY  CLUSTERED 
	(
		infoId
	)   
;

ALTER TABLE SB_Contact_InfoAttachment WITH NOCHECK ADD 
	 CONSTRAINT PK_Contact_InfoAttachment PRIMARY KEY  CLUSTERED 
	(
		infoAttachmentId
	)   
;

ALTER TABLE SB_Contact_InfoImage WITH NOCHECK ADD 
	 CONSTRAINT PK_Contact_InfoImage PRIMARY KEY  CLUSTERED 
	(
		infoImageId
	)   
;

ALTER TABLE SB_Contact_InfoLink WITH NOCHECK ADD 
	 CONSTRAINT PK_Contact_InfoLink PRIMARY KEY  CLUSTERED 
	(
		infoLinkId
	)   
;

ALTER TABLE SB_Contact_InfoText WITH NOCHECK ADD 
	 CONSTRAINT PK_Contact_InfoText PRIMARY KEY  CLUSTERED 
	(
		infoTextId
	)   
;