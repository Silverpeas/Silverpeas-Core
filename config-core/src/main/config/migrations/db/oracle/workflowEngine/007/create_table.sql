CREATE TABLE SB_Workflow_ProcessInstance
(
	instanceId	int		NOT NULL ,
	modelId		varchar (50)	NOT NULL ,
	locked		numeric(1)		NOT NULL ,
	errorStatus	numeric(1)		DEFAULT 0
                              CONSTRAINT NN_WF_INST_ERROR NOT NULL,
	timeoutStatus	numeric(1)	DEFAULT 0
                              CONSTRAINT NN_WF_INST_TIMEOUT NOT NULL
)
;

CREATE TABLE SB_Workflow_ActiveState
(
	id		int NOT NULL ,
	instanceId	int NOT NULL ,
	state		varchar (50) ,
	backStatus	numeric(1) DEFAULT 0
                          CONSTRAINT NN_WF_STATE_ERROR NOT NULL,
	timeoutStatus	numeric(1) DEFAULT 0
                          CONSTRAINT NN_WF_STATE_TIMEOUT NOT NULL,
    timeoutDate	timestamp
)
;

CREATE TABLE SB_Workflow_HistoryStep
(
	instanceId    int NOT NULL ,
	id            int NOT NULL ,
	userId        varchar (50) ,
	userRoleName  varchar (50) ,
	action        varchar (50) ,
	actionDate    timestamp ,
	resolvedState varchar (50) ,
	toState       varchar (50) ,
	actionStatus  int
)
;

CREATE TABLE SB_Workflow_Undo_Step
(
	id         int NOT NULL ,
	stepId     int NOT NULL ,
	instanceId int NOT NULL ,
	action     varchar (20) NOT NULL ,
	parameters varchar (150) NOT NULL
)
;

CREATE TABLE SB_Workflow_InterestedUser
(
	id         int NOT NULL ,
	userId     varchar (50) NULL ,
	usersrole  varchar (50) NULL ,
	instanceId int NOT NULL ,
	state      varchar (50) ,
	role       varchar (50) NOT NULL,
	groupid    varchar (50) NULL
)
;

CREATE TABLE SB_Workflow_LockingUser
(
	id         int NOT NULL ,
	userId     varchar (50) NOT NULL ,
	instanceId int NOT NULL ,
	state      varchar (50) ,
	lockDate   timestamp
)
;

CREATE TABLE SB_Workflow_WorkingUser
(
	id         int NOT NULL ,
	userId     varchar (50) NULL ,
	usersrole  varchar (50) NULL ,
	instanceId int NOT NULL ,
	state      varchar (50) ,
	role       varchar (50) NOT NULL,
	groupid    varchar (50) NULL
)
;

CREATE TABLE SB_Workflow_Question
(
	id		int NOT NULL ,
	instanceId	int NOT NULL ,
	questionText	varchar (500) NOT NULL ,
	responseText	varchar (500) NULL ,
	questionDate	timestamp NOT NULL ,
	responseDate	timestamp NULL ,
	fromState	varchar (50) NOT NULL ,
	targetState	varchar (50) NOT NULL ,
	fromUserId	varchar (50) NOT NULL ,
	toUserId	varchar (50) NOT NULL ,
	relevant	numeric(1) NOT NULL
)
;

CREATE TABLE SB_Workflow_UserInfo
(
	id		int NOT NULL ,
	settingsId	int NOT NULL ,
	name		varchar (50) NOT NULL ,
	value		varchar (100) NULL
)
;

CREATE TABLE SB_Workflow_UserSettings
(
	settingsId	int NOT NULL ,
	userId		varchar (100) NOT NULL ,
	peasId		varchar (100) NOT NULL
)
;

CREATE TABLE SB_Workflow_Error
(
	id		int		not null,
	instanceId	int		not null,
	stepId		int		null,
	errorMessage	varchar (200)	null,
	stackTrace	varchar (4000)	null,
	userId		varchar (100)	null,
	actionName	varchar (100)	null,
	actionDate	timestamp	null,
	userRole	varchar (100)	null,
	stateName	varchar (100)	null
)
;
