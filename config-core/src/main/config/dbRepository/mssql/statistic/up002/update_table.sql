drop index SB_Statistic_History.IND_Statistic_ObjectId

EXEC sp_rename 'SB_Statistic_History.objectId','resourceId','COLUMN';

ALTER TABLE SB_Statistic_History
ALTER COLUMN resourceId VARCHAR(50) NOT NULL
;

EXEC sp_rename 'SB_Statistic_History.objectType','resourceType','COLUMN';

create index IND_Statistic_ResourceId on SB_Statistic_History (resourceId)