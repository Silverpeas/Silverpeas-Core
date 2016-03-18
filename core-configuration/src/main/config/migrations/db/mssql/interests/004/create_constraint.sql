ALTER TABLE SB_Interests ADD
	 CONSTRAINT PK_Interests PRIMARY KEY  CLUSTERED
	(
		id
	)
;

ALTER TABLE SB_Interests_Axis ADD
	 CONSTRAINT PK_Interest_Center_Axis PRIMARY KEY  CLUSTERED
	(
		id
	)
;