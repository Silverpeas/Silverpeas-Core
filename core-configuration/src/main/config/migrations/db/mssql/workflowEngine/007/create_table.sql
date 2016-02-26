CREATE TABLE SB_Workflow_ProcessInstance
(
	instanceId	int		NOT NULL ,
	modelId		varchar (50)	NOT NULL ,
	locked		bit		NOT NULL ,
	errorStatus	bit		NOT NULL DEFAULT 0,
	timeoutStatus	bit		NOT NULL DEFAULT 0
)
;

CREATE TABLE SB_Workflow_ActiveState
(
	id		int NOT NULL ,
	instanceId	int NOT NULL ,
	state		varchar (50) NOT NULL ,
	backStatus	bit NOT NULL DEFAULT 0,
	timeoutStatus	bit NOT NULL DEFAULT 0,
	timeoutDate	datetime NULL
)
;

CREATE TABLE SB_Workflow_HistoryStep
(
	instanceId    int NOT NULL ,
	id            int NOT NULL ,
	userId        varchar (50) ,
	userRoleName  varchar (50) ,
	action        varchar (50) ,
	actionDate    datetime ,
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
	state      varchar (50) NOT NULL ,
	role       varchar (50) NOT NULL,
	groupid    varchar (50) NULL
)
;

CREATE TABLE SB_Workflow_LockingUser
(
	id         int NOT NULL ,
	userId     varchar (50) NOT NULL ,
	instanceId int NOT NULL ,
	state      varchar (50) NOT NULL ,
	lockDate   datetime
)
;

CREATE TABLE SB_Workflow_WorkingUser
(
	id         int NOT NULL ,
	userId     varchar (50) NULL ,
	usersrole  varchar (50) NULL ,
	instanceId int NOT NULL ,
	state      varchar (50) NOT NULL ,
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
	questionDate	datetime NOT NULL ,
	responseDate	datetime NULL ,
	fromState	varchar (50) NOT NULL ,
	targetState	varchar (50) NOT NULL ,
	fromUserId	varchar (50) NOT NULL ,
	toUserId	varchar (50) NOT NULL ,
	relevant	bit NOT NULL
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
	actionDate	datetime	null,
	userRole	varchar (100)	null,
	stateName	varchar (100)	null
)
;
