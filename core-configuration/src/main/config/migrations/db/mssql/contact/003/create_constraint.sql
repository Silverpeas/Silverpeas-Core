ALTER TABLE SB_Contact_Contact WITH NOCHECK ADD
	 CONSTRAINT PK_Contact_Contact PRIMARY KEY CLUSTERED
	(
		contactId
	)
;

ALTER TABLE SB_Contact_contactfather WITH NOCHECK ADD
	 CONSTRAINT PK_Contact_contactfather PRIMARY KEY  CLUSTERED
	(
		contactId,
		nodeId
	)
;

ALTER TABLE SB_Contact_Info WITH NOCHECK ADD
	 CONSTRAINT PK_Contact_Info PRIMARY KEY  CLUSTERED
	(
		infoId
	)
;
