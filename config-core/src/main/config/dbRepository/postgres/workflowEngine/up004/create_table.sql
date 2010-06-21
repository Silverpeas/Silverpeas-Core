ALTER TABLE SB_Workflow_InterestedUser
ADD groupid varchar (50) NULL
;

ALTER TABLE SB_Workflow_WorkingUser
ADD groupid varchar (50) NULL
;

ALTER TABLE SB_Workflow_Error
ALTER COLUMN actiondate TYPE timestamp
;

ALTER TABLE SB_Workflow_HistoryStep
ALTER COLUMN actiondate TYPE timestamp
;

ALTER TABLE SB_Workflow_LockingUser
ALTER COLUMN lockdate TYPE timestamp
;

ALTER TABLE SB_Workflow_Question
ALTER COLUMN questiondate TYPE timestamp
;

ALTER TABLE SB_Workflow_Question
ALTER COLUMN responsedate TYPE timestamp
;
