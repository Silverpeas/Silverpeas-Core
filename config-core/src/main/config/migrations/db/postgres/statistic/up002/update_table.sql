drop index IND_Statistic_ObjectId
;

ALTER TABLE SB_Statistic_History
RENAME objectId TO resourceId
;

ALTER TABLE SB_Statistic_History
ALTER COLUMN resourceId TYPE VARCHAR(50)
;

ALTER TABLE SB_Statistic_History
RENAME objectType TO resourceType
;

create index IND_Statistic_ResourceId on SB_Statistic_History (resourceId)
;