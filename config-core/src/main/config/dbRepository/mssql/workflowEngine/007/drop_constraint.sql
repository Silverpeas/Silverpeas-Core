ALTER TABLE SB_Workflow_ActiveState DROP 
	CONSTRAINT FK_Workflow_ActiveState
;

ALTER TABLE SB_Workflow_HistoryStep DROP 
	CONSTRAINT FK_Workflow_HistoryStep
;

ALTER TABLE SB_Workflow_InterestedUser DROP 
	CONSTRAINT FK_Workflow_InterestedUser
;

ALTER TABLE SB_Workflow_LockingUser DROP 
	CONSTRAINT FK_Workflow_LockingUser
;

ALTER TABLE SB_Workflow_WorkingUser DROP 
	CONSTRAINT FK_Workflow_WorkingUser
;

ALTER TABLE SB_Workflow_Question DROP
	CONSTRAINT FK_Workflow_Question
;

ALTER TABLE SB_Workflow_UserInfo DROP 
	CONSTRAINT FK_Workflow_UserInfo
;

ALTER TABLE SB_Workflow_Question DROP 
	CONSTRAINT PK_Workflow_Question
;

ALTER TABLE SB_Workflow_UserInfo DROP
	CONSTRAINT PK_Workflow_UserInfo
;

ALTER TABLE SB_Workflow_UserSettings DROP
	CONSTRAINT PK_Workflow_UserSettings
;

ALTER TABLE SB_Workflow_ActiveState DROP 
	CONSTRAINT PK_Workflow_ActiveState
;

ALTER TABLE SB_Workflow_HistoryStep DROP 
	CONSTRAINT PK_Workflow_HistoryStep
;

ALTER TABLE SB_Workflow_InterestedUser DROP 
	CONSTRAINT PK_Workflow_InterestedUser
;

ALTER TABLE SB_Workflow_LockingUser DROP 
	CONSTRAINT PK_Workflow_LockingUser
;

ALTER TABLE SB_Workflow_ProcessInstance DROP 
	CONSTRAINT PK_Workflow_ProcessInstance
;

ALTER TABLE SB_Workflow_Undo_Step DROP 
	CONSTRAINT PK_Workflow_Undo_Step
;

ALTER TABLE SB_Workflow_WorkingUser DROP 
	CONSTRAINT PK_Workflow_WorkingUser
;

ALTER TABLE SB_Workflow_Error DROP 
	CONSTRAINT PK_Workflow_Error
;
