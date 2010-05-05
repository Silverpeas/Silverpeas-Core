ALTER TABLE st_instance_modelused
DROP CONSTRAINT pk_sc_kmelia_modelused
;

ALTER TABLE st_instance_modelused 
ADD COLUMN objectId varchar(50) DEFAULT ('0') NOT NULL
;

update st_instance_modelused
set objectId = '0'
;

ALTER TABLE st_instance_modelused ADD 
	 CONSTRAINT PK_st_instance_modelused PRIMARY KEY 
	(
		instanceId,
		modelId,
		objectId
	)
;