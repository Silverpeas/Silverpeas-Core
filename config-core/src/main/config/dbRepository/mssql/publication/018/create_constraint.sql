ALTER TABLE SB_Publication_Info WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_Info PRIMARY KEY  CLUSTERED 
	(
		infoId
	)   
;

ALTER TABLE SB_Publication_InfoAttachment WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_InfoAttachment PRIMARY KEY  CLUSTERED 
	(
		infoAttachmentId
	)   
;

ALTER TABLE SB_Publication_InfoImage WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_InfoImage PRIMARY KEY  CLUSTERED 
	(
		infoImageId
	)   
;

ALTER TABLE SB_Publication_InfoLink WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_InfoLink PRIMARY KEY  CLUSTERED 
	(
		infoLinkId
	)   
;

ALTER TABLE SB_Publication_InfoText WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_InfoText PRIMARY KEY  CLUSTERED 
	(
		infoTextId
	)   
;

ALTER TABLE SB_Publication_Publi WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_Publi PRIMARY KEY  CLUSTERED 
	(
		pubId
	)   
;

ALTER TABLE SB_Publication_PubliFather WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_PubliFather PRIMARY KEY  CLUSTERED 
	(
		pubId,
		nodeId,
		instanceId
	)   
;

ALTER TABLE SB_SeeAlso_Link WITH NOCHECK ADD 
	 CONSTRAINT PK_SeeAlso_Link PRIMARY KEY CLUSTERED  
	(
		id
	)   
;

ALTER TABLE SB_Publication_PubliI18N WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_PubliI18N PRIMARY KEY CLUSTERED
	(
		id
	)   
;

ALTER TABLE SB_Thumbnail_Thumbnail WITH NOCHECK ADD 
	 CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY CLUSTERED 
	(
		objectId,
		objectType,
		instanceId
	)   
;