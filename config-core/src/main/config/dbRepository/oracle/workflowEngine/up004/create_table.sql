ALTER TABLE SB_Workflow_InterestedUser
ADD groupid varchar (50) NULL
;

ALTER TABLE SB_Workflow_WorkingUser
ADD groupid varchar (50) NULL
;

ALTER TABLE SB_Workflow_Error
MODIFY actiondate timestamp
;

ALTER TABLE SB_Workflow_HistoryStep
MODIFY actiondate timestamp
;

ALTER TABLE SB_Workflow_LockingUser
MODIFY lockdate timestamp
;

ALTER TABLE SB_Workflow_Question
MODIFY questiondate timestamp
;

ALTER TABLE SB_Workflow_Question
MODIFY responsedate timestamp
;




