EXEC sp_rename 'SB_Comment_Comment.foreignId','resourceId','COLUMN';

ALTER TABLE SB_Comment_Comment
ALTER COLUMN resourceId VARCHAR(50) NOT NULL
;

ALTER TABLE SB_Comment_Comment
ADD resourceType VARCHAR (50) NULL
;

-- Data recovery
UPDATE SB_Comment_Comment
SET resourceType =
	CASE WHEN resourceId = '-1' AND instanceId LIKE 'projectManager%'
		THEN	'ProjectManager'
	WHEN resourceId != '-1' AND instanceId LIKE 'projectManager%'
		THEN	'Task'
	WHEN instanceId LIKE 'resourcesManager%'
		THEN	'ResourcesManager'
	WHEN instanceId LIKE 'kmelia%'
		THEN	'Publication'
	WHEN instanceId LIKE 'gallery%'
		THEN	'Photo'
	WHEN instanceId LIKE 'classifieds%'
		THEN	'Classified'
	WHEN instanceId LIKE 'blog%'
		THEN	'Publication'
	ELSE
		'#null#'
	END
;

-- Ending structure modifications
ALTER TABLE SB_Comment_Comment
ALTER COLUMN resourceType VARCHAR(50) NOT NULL
;