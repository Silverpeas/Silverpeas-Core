ALTER TABLE SB_Coordinates_Coordinates WITH NOCHECK ADD
	 CONSTRAINT PK_Coordinates_Coordinates PRIMARY KEY  CLUSTERED
	(
		coordinatesId,
		nodeId,
		instanceId
	)
;