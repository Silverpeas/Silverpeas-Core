ALTER TABLE SB_Workflow_ProcessInstance
ADD (errorStatus numeric(1) DEFAULT 0)
;
ALTER TABLE SB_Workflow_ProcessInstance
MODIFY errorStatus NOT NULL
;

ALTER TABLE SB_Workflow_ProcessInstance
ADD (timeoutStatus numeric(1) DEFAULT 0)
;
ALTER TABLE SB_Workflow_ProcessInstance
MODIFY timeoutStatus NOT NULL
;

ALTER TABLE SB_Workflow_ActiveState
ADD (timeoutStatus numeric(1) DEFAULT 0)
;
ALTER TABLE SB_Workflow_ActiveState
MODIFY timeoutStatus NOT NULL
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
	actionDate	date	null,
	userRole	varchar (100)	null,
	stateName	varchar (100)	null
)
;

ALTER TABLE SB_Workflow_Error ADD 
	CONSTRAINT PK_Workflow_Error
	PRIMARY KEY (id)
;
