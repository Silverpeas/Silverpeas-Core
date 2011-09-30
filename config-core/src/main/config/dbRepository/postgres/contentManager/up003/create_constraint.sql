ALTER TABLE SB_ContentManager_Content ADD 
	 CONSTRAINT UQE_ContentManager_Content UNIQUE
	(
		internalContentId, contentInstanceId
	)   
;