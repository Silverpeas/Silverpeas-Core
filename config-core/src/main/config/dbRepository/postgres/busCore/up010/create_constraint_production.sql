ALTER TABLE model DROP CONSTRAINT PK_model;

ALTER TABLE model  ADD 
	CONSTRAINT PK_model PRIMARY KEY   
	(
		id, partId
	);