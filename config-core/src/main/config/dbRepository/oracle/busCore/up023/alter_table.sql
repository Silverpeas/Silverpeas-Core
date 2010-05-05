ALTER TABLE st_instance_modelused 
DROP CONSTRAINT PK_st_instance_modelused
;

ALTER TABLE st_instance_modelused 
ADD objectId varchar(50) DEFAULT ('0') NOT NULL
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