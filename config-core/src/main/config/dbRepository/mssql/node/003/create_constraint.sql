ALTER TABLE SB_Node_Node WITH NOCHECK ADD 
	 CONSTRAINT PK_Node_Node PRIMARY KEY  CLUSTERED 
	(
		nodeId,
		instanceId
	)   
;