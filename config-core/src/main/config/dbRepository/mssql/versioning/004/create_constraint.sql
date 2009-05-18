ALTER TABLE SB_Version_Document WITH NOCHECK ADD 
	 CONSTRAINT PK_Version_Document PRIMARY KEY  CLUSTERED 
	(
		documentid
	)
;

ALTER TABLE SB_Version_Version WITH NOCHECK ADD 
	 CONSTRAINT PK_Version_Version PRIMARY KEY  CLUSTERED 
	(
		versionid
	)
;
