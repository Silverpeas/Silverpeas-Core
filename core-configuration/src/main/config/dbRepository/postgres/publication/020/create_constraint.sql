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