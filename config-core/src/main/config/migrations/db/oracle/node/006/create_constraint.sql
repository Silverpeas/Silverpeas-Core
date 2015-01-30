ALTER TABLE SB_Node_Node ADD
	 CONSTRAINT PK_Node_Node
	 PRIMARY KEY
	(
		nodeId,
		instanceId
	)
;
ALTER TABLE SB_Node_NodeI18N ADD
	 CONSTRAINT PK_Node_NodeI18N
	 PRIMARY KEY
	(
		id
	)
;
