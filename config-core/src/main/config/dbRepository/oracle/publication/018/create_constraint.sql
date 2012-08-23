ALTER TABLE SB_Publication_Info ADD 
	 CONSTRAINT PK_Publication_Info PRIMARY KEY   
	(
		infoId
	)   
;

ALTER TABLE SB_Publication_InfoAttachment  ADD 
	 CONSTRAINT PK_Publication_InfoAttachment PRIMARY KEY   
	(
		infoAttachmentId
	)   
;

ALTER TABLE SB_Publication_InfoImage  ADD 
	 CONSTRAINT PK_Publication_InfoImage PRIMARY KEY   
	(
		infoImageId
	)   
;

ALTER TABLE SB_Publication_InfoLink  ADD 
	 CONSTRAINT PK_Publication_InfoLink PRIMARY KEY   
	(
		infoLinkId
	)   
;

ALTER TABLE SB_Publication_InfoText  ADD 
	 CONSTRAINT PK_Publication_InfoText PRIMARY KEY   
	(
		infoTextId
	)   
;

ALTER TABLE SB_Publication_Publi  ADD 
	 CONSTRAINT PK_Publication_Publi PRIMARY KEY   
	(
		pubId
	)   
;

ALTER TABLE SB_Publication_PubliFather  ADD 
	 CONSTRAINT PK_Publication_PubliFather PRIMARY KEY   
	(
		pubId,
		nodeId,
		instanceId
	)   
;

ALTER TABLE SB_SeeAlso_Link  ADD 
	 CONSTRAINT PK_SeeAlso_Link PRIMARY KEY   
	(
		id
	)   
;

ALTER TABLE SB_Publication_PubliI18N  ADD 
	 CONSTRAINT PK_Publication_PubliI18N PRIMARY KEY   
	(
		id
	)
;

ALTER TABLE SB_Thumbnail_Thumbnail ADD 
	 CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY   
	(
		objectId,
		objectType,
		instanceId
	)   
;