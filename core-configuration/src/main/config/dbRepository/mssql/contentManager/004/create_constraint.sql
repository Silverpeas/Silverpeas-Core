ALTER TABLE SB_ContentManager_Instance ADD
	 CONSTRAINT PK_ContentManager_Instance PRIMARY KEY
	(
		instanceId
	)
;

ALTER TABLE SB_ContentManager_Content ADD
	 CONSTRAINT PK_ContentManager_Content PRIMARY KEY
	(
		silverContentId
	)
;

ALTER TABLE SB_ContentManager_Content ADD
	 CONSTRAINT UQE_ContentManager_Content UNIQUE
	(
		internalContentId, contentInstanceId
	)
;