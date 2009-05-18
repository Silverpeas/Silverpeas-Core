ALTER TABLE SB_Publication_PubliFather
DROP CONSTRAINT PK_Publication_PubliFather;

ALTER TABLE SB_Publication_PubliFather  ADD 
	 CONSTRAINT PK_Publication_PubliFather PRIMARY KEY   
	(
		pubId,
		nodeId,
		instanceId
	)   
;