ALTER TABLE SB_Workflow_ProcessInstance
ADD errorStatus bit NOT NULL DEFAULT 0
;

ALTER TABLE SB_Workflow_ProcessInstance
ADD timeoutStatus bit NOT NULL DEFAULT 0
;

ALTER TABLE SB_Workflow_ActiveState
ADD timeoutStatus bit NOT NULL DEFAULT 0
;

CREATE TABLE SB_Workflow_Error
(
	id		int		not null,
	instanceId	int		not null,
	stepId		int		null,
	errorMessage	varchar (200)	null,
	stackTrace	varchar (1500)	null,
	userId		varchar (100)	null,
	actionName	varchar (100)	null,
	actionDate	datetime	null,
	userRole	varchar (100)	null,
	stateName	varchar (100)	null
)
;

ALTER TABLE SB_Workflow_Error ADD 
	CONSTRAINT PK_Workflow_Error
	PRIMARY KEY (id)
;
