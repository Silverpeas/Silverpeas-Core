ALTER TABLE st_instance_modelused 
DROP CONSTRAINT PK_st_instance_modelused
;

ALTER TABLE st_instance_modelused 
ADD objectId varchar(50) NOT NULL DEFAULT ('0')
;

update st_instance_modelused
set objectId = '0'
;

ALTER TABLE st_instance_modelused WITH NOCHECK ADD 
	 CONSTRAINT PK_st_instance_modelused PRIMARY KEY  CLUSTERED 
	(
		instanceId,
		modelId,
		objectId
	)
;