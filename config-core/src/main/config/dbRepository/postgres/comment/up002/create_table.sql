ALTER TABLE SB_Comment_Comment
RENAME foreignId TO resourceId
;

ALTER TABLE SB_Comment_Comment
ALTER COLUMN resourceId TYPE VARCHAR(50)
;
ALTER TABLE SB_Comment_Comment
ADD resourceType VARCHAR(50) null
;

-- Data recovery
UPDATE SB_Comment_Comment
SET resourceType = '#null#';

-- Ending structure modifications
ALTER TABLE SB_Comment_Comment
ALTER COLUMN resourceType SET NOT NULL
;