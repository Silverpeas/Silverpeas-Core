drop index IND_Statistic_ObjectId
;

ALTER TABLE SB_Statistic_History
ADD (resourceId VARCHAR2(50))
;

UPDATE SB_Statistic_History
SET resourceId = objectId
;

ALTER TABLE SB_Statistic_History
MODIFY (resourceId VARCHAR2(50) NOT NULL)
;

ALTER TABLE SB_Statistic_History
DROP COLUMN objectId
;

ALTER TABLE SB_Statistic_History
ADD (resourceType VARCHAR2(50))
;

UPDATE SB_Statistic_History
SET resourceType = objectType
;

ALTER TABLE SB_Statistic_History
MODIFY (resourceType VARCHAR2(50) NOT NULL)
;

ALTER TABLE SB_Statistic_History
DROP COLUMN objectType
;

create index IND_Statistic_ResourceId on SB_Statistic_History (resourceId)
;