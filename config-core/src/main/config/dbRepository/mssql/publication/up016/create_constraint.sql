ALTER TABLE SB_Publication_PubliFather
DROP CONSTRAINT PK_Publication_PubliFather;

ALTER TABLE SB_Publication_PubliFather WITH NOCHECK ADD 
	 CONSTRAINT PK_Publication_PubliFather PRIMARY KEY CLUSTERED 
	(
		pubId,
		nodeId,
		instanceId
	)   
;