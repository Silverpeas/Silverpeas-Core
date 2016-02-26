ALTER TABLE SB_Workflow_ActiveState ADD
	CONSTRAINT PK_Workflow_ActiveState
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_HistoryStep ADD
	CONSTRAINT PK_Workflow_HistoryStep
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_InterestedUser ADD
	CONSTRAINT PK_Workflow_InterestedUser
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_LockingUser ADD
	CONSTRAINT PK_Workflow_LockingUser
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_ProcessInstance ADD
	CONSTRAINT PK_Workflow_ProcessInstance
	PRIMARY KEY ( instanceId )
;

ALTER TABLE SB_Workflow_Undo_Step ADD
	CONSTRAINT PK_Workflow_Undo_Step
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_WorkingUser ADD
	CONSTRAINT PK_Workflow_WorkingUser
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_Question ADD
	CONSTRAINT PK_Workflow_Question
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_UserInfo ADD
	CONSTRAINT PK_Workflow_UserInfo
	PRIMARY KEY ( id )
;

ALTER TABLE SB_Workflow_UserSettings ADD
	CONSTRAINT PK_Workflow_UserSettings
	PRIMARY KEY ( settingsId )
;

ALTER TABLE SB_Workflow_Error ADD
	CONSTRAINT PK_Workflow_Error
	PRIMARY KEY (id)
;

ALTER TABLE SB_Workflow_ActiveState ADD
	CONSTRAINT FK_Workflow_ActiveState
	FOREIGN KEY ( instanceId )
	REFERENCES SB_Workflow_ProcessInstance ( instanceId )
;

ALTER TABLE SB_Workflow_HistoryStep ADD
	CONSTRAINT FK_Workflow_HistoryStep
	FOREIGN KEY ( instanceId )
	REFERENCES SB_Workflow_ProcessInstance ( instanceId )
;

ALTER TABLE SB_Workflow_InterestedUser ADD
	CONSTRAINT FK_Workflow_InterestedUser
	FOREIGN KEY ( instanceId )
	REFERENCES SB_Workflow_ProcessInstance ( instanceId )
;

ALTER TABLE SB_Workflow_LockingUser ADD
	CONSTRAINT FK_Workflow_LockingUser
	FOREIGN KEY ( instanceId )
	REFERENCES SB_Workflow_ProcessInstance ( instanceId )
;

ALTER TABLE SB_Workflow_WorkingUser ADD
	CONSTRAINT FK_Workflow_WorkingUser
	FOREIGN KEY ( instanceId )
	REFERENCES SB_Workflow_ProcessInstance ( instanceId )
;

ALTER TABLE SB_Workflow_Question ADD
	CONSTRAINT FK_Workflow_Question
	FOREIGN KEY ( instanceId )
	REFERENCES SB_Workflow_ProcessInstance ( instanceId )
;

ALTER TABLE SB_Workflow_UserInfo ADD
	CONSTRAINT FK_Workflow_UserInfo
	FOREIGN KEY ( settingsId )
	REFERENCES SB_Workflow_UserSettings ( settingsId )
;
