ALTER TABLE SB_fileSharing_ticket
ADD CONSTRAINT PK_SB_fileSharing_ticket PRIMARY KEY
	(
		keyFile
	)
;

ALTER TABLE SB_fileSharing_history
ADD CONSTRAINT PK_SB_fileSharing_history PRIMARY KEY
	(
		id
	)
;
