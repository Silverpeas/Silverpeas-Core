ALTER TABLE SB_Contact_Contact  ADD
	 CONSTRAINT PK_Contact_Contact PRIMARY KEY
	(
		contactId
	)
;

ALTER TABLE SB_Contact_Contactfather  ADD
	 CONSTRAINT PK_Contact_contactfather PRIMARY KEY
	(
		contactId,
		nodeId
	)
;

ALTER TABLE SB_Contact_Info  ADD
	 CONSTRAINT PK_Contact_Info PRIMARY KEY
	(
		infoId
	)
;
