ALTER TABLE SB_Contact_Contact  ADD 
	 CONSTRAINT PK_Contact_Contact PRIMARY KEY  
	(
		contactId
	)   
;

ALTER TABLE SB_Contact_Contactfather  ADD 
	 CONSTRAINT PK_Contact_contactfather PRIMARY KEY   
	(
		contactId,
		nodeId
	)   
;

ALTER TABLE SB_Contact_Info  ADD 
	 CONSTRAINT PK_Contact_Info PRIMARY KEY   
	(
		infoId
	)   
;

ALTER TABLE SB_Contact_InfoAttachment  ADD 
	 CONSTRAINT PK_Contact_InfoAttachment PRIMARY KEY   
	(
		infoAttachmentId
	)   
;

ALTER TABLE SB_Contact_InfoImage  ADD 
	 CONSTRAINT PK_Contact_InfoImage PRIMARY KEY   
	(
		infoImageId
	)   
;

ALTER TABLE SB_Contact_InfoLink  ADD 
	 CONSTRAINT PK_Contact_InfoLink PRIMARY KEY   
	(
		infoLinkId
	)   
;

ALTER TABLE SB_Contact_InfoText  ADD 
	 CONSTRAINT PK_Contact_InfoText PRIMARY KEY   
	(
		infoTextId
	)   
;