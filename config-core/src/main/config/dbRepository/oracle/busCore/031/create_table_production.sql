CREATE TABLE UniqueId (
	maxId NUMBER(19, 0) NOT NULL ,
	tableName varchar2(100) NOT NULL
);

CREATE TABLE Personalization (
	id varchar2(100) NOT NULL ,
	languages varchar2(100) NULL,
	look varchar2(50) NULL,
	personalWSpace varchar2(50) NULL,
	thesaurusStatus int NOT NULL,
	dragAndDropStatus int default 1 NOT NULL,
	webdavEditingStatus int default 0 NOT NULL,
	menuDisplay varchar2(50) DEFAULT 'DEFAULT'
);


CREATE TABLE readingControl (
	pubId int NOT NULL ,
	actorId varchar2(100) NOT NULL ,
	space varchar2(50) NOT NULL ,
	componentName varchar2(50) NOT NULL
);

CREATE TABLE subscribe (
  subscriberId       VARCHAR2(100) NOT NULL,
  subscriberType     VARCHAR2(50)  NOT NULL,
  subscriptionMethod VARCHAR2(50)  NOT NULL,
  resourceId         VARCHAR2(100) NOT NULL,
  resourceType       VARCHAR2(50)  NOT NULL,
  space              VARCHAR2(50)  NOT NULL,
  instanceId         VARCHAR2(50)  NOT NULL,
  creatorId          VARCHAR2(100) NOT NULL,
  creationDate       TIMESTAMP     NOT NULL
);


CREATE TABLE model_contact (
	id int NOT NULL ,
	name varchar2(50) NOT NULL ,
	description varchar2(50) NULL ,
	imageName varchar2(50) NULL ,
	htmlDisplayer varchar2(3000) NOT NULL ,
	htmlEditor varchar2(3000) NOT NULL
);


CREATE TABLE model (
	id int NOT NULL ,
	name varchar2(50) NOT NULL ,
	description varchar2(100) NULL ,
	imageName varchar2(100) NULL ,
	htmlDisplayer varchar2(3500) NOT NULL ,
	htmlEditor varchar2(3500) NOT NULL ,
	partId int default 1 NOT NULL
);

CREATE TABLE favorit (
	actorId varchar2(100) NOT NULL ,
	nodeId int NOT NULL ,
	space varchar2(50) NOT NULL ,
	componentName varchar2(50) NOT NULL
);

CREATE TABLE calendarJournal (
	id int NOT NULL ,
	name varchar2(2000) NOT NULL ,
	description varchar2(4000) NULL ,
	delegatorId varchar2(100) NOT NULL ,
	startDay varchar2(50) NOT NULL ,
	endDay varchar2(50) NULL ,
	startHour varchar2(50) NULL ,
	endHour varchar2(50) NULL ,
	classification varchar2(20) NULL ,
	priority int NULL ,
	lastModification varchar2(50) NULL,
	externalid varchar2(50) NULL
);

CREATE TABLE calendarCategory (
	categoryId varchar2(50) NOT NULL ,
	name varchar2(50) NOT NULL
);

CREATE TABLE calendarJournalAttendee (
	journalId int NOT NULL ,
	userId varchar2(100) NOT NULL ,
	participationStatus varchar2(50) NULL
);

CREATE TABLE calendarJournalCategory (
	journalId int NOT NULL ,
	categoryId varchar2(50) NOT NULL
);

CREATE TABLE days (
	day varchar2(50) NOT NULL
);

CREATE TABLE calendarToDo (
	id int NOT NULL ,
	name varchar2(2000) NOT NULL ,
	description varchar2(4000) NULL ,
	delegatorId varchar2(100) NOT NULL ,
	startDay varchar2(50) NULL ,
	endDay varchar2(50) NULL ,
	startHour varchar2(50) NULL ,
	endHour varchar2(50) NULL ,
	classification varchar2(20) NULL ,
	priority int NULL ,
	lastModification varchar2(50) NULL ,
	percentCompleted int NULL ,
	completedDay varchar2(20) NULL ,
	duration int NULL ,
	componentId varchar2(100) NULL ,
	spaceId varchar2(100) NULL ,
	externalId varchar2(100) NULL
);


CREATE TABLE calendarToDoAttendee (
	todoId int NOT NULL ,
	userId varchar2(100) NOT NULL ,
	participationStatus varchar2(50) NULL
);

CREATE TABLE ST_FormDesigner_FormDesign (
	ID int NOT NULL ,
	REFIDFORM int NOT NULL ,
	COMPONENTID varchar2(100) NOT NULL ,
	NAME varchar2(1000) NOT NULL ,
	DESCRIPTION varchar2(2000) NOT NULL ,
	CREATIONDATE varchar2(10) NOT NULL ,
	AUTHOR int  NOT NULL
);

CREATE TABLE ST_FormDesigner_Connectors (
	ID int NOT NULL ,
	NAME varchar2(1000) NOT NULL ,
	DESCRIPTION varchar2(2000) NOT NULL,
	DRIVER varchar2(1000) NOT NULL,
	URL varchar2(1000) NOT NULL,
	LOGIN varchar2(1000) NOT NULL,
	PASSWD varchar2(1000) NULL,
	SQLQUERY varchar2(4000) NOT NULL,
	TYPE varchar2(50) NOT NULL
);

CREATE TABLE ST_FormEditor_FormEdited (
	ID int NOT NULL ,
	FORMID int NOT NULL ,
	USERID int NOT NULL,
	CREATEDATE varchar2(10) NOT NULL,
	MODIFYDATE varchar2(10) NOT NULL
);

CREATE TABLE ST_FormEditor_FormEditedData (
	ID int NOT NULL ,
	FORMEDITEDID int NOT NULL ,
	EDITEDKEY varchar2(50) NOT NULL,
	EDITEDVALUE varchar2(500) NOT NULL
);

CREATE TABLE sb_agenda_import_settings (
  userid int NOT NULL,
  hostname varchar2(500) NOT NULL,
  synchrotype int NOT NULL,
  synchrodelay int NOT NULL,
  url varchar2(500) NULL,
  remotelogin varchar2(200) NULL,
  remotepwd varchar2(200) NULL,
  charset varchar2(20) NULL
);