ALTER TABLE SB_Comment_Comment
RENAME foreignId TO resourceId
;

ALTER TABLE SB_Comment_Comment
ALTER COLUMN resourceId TYPE VARCHAR(50)
;

ALTER TABLE SB_Comment_Comment
ADD resourceType VARCHAR (50)
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
ALTER COLUMN resourceType SET NOT NULL
;