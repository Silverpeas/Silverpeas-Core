CREATE TABLE UniqueId (
	maxId BIGINT NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE Personalization (
	id varchar(100) NOT NULL ,
	languages varchar(100) NULL,
	look varchar(50) NULL,
	personalWSpace varchar(50) NULL,
	thesaurusStatus int NOT NULL,
	dragAndDropStatus int NOT NULL default 1,
  webdavEditingStatus int NOT NULL default 0,
	menuDisplay varchar(50) DEFAULT 'DEFAULT'
);


CREATE TABLE readingControl (
	pubId int NOT NULL ,
	actorId varchar(100) NOT NULL ,
	space varchar(50) NOT NULL ,
	componentName varchar(50) NOT NULL
);

CREATE TABLE subscribe (
  subscriberId       VARCHAR(100) NOT NULL,
  subscriberType     VARCHAR(50)  NOT NULL,
  subscriptionMethod VARCHAR(50)  NOT NULL,
  resourceId         VARCHAR(100) NOT NULL,
  resourceType       VARCHAR(50)  NOT NULL,
  space              VARCHAR(50)  NOT NULL,
  instanceId         VARCHAR(50)  NOT NULL,
  creatorId          VARCHAR(100) NOT NULL,
  creationDate       DATETIME     NOT NULL
);


CREATE TABLE model_contact (
	id int NOT NULL ,
	name varchar(50) NOT NULL ,
	description varchar(50) NULL ,
	imageName varchar(50) NULL ,
	htmlDisplayer varchar(3000) NOT NULL ,
	htmlEditor varchar(3000) NOT NULL
);


CREATE TABLE model (
	id int NOT NULL ,
	name varchar(50) NOT NULL ,
	description varchar(100) NULL ,
	imageName varchar(100) NULL ,
	htmlDisplayer varchar(3500) NOT NULL ,
	htmlEditor varchar(3500) NOT NULL ,
	partId int NOT NULL default 1
);

CREATE TABLE favorit (
	actorId varchar(100) NOT NULL ,
	nodeId int NOT NULL ,
	space varchar(50) NOT NULL ,
	componentName varchar(50) NOT NULL
);

CREATE TABLE calendarJournal (
	id int NOT NULL ,
	name varchar(2000) NOT NULL ,
	description varchar(4000) NULL ,
	delegatorId varchar(100) NOT NULL ,
	startDay varchar(50) NOT NULL ,
	endDay varchar(50) NULL ,
	startHour varchar(50) NULL ,
	endHour varchar(50) NULL ,
	classification varchar(20) NULL ,
	priority int NULL ,
	lastModification varchar(50) NULL,
	externalid varchar(50) NULL
);

CREATE TABLE calendarCategory (
	categoryId varchar(50) NOT NULL ,
	name varchar(50) NOT NULL
);

CREATE TABLE calendarJournalAttendee (
	journalId int NOT NULL ,
	userId varchar(100) NOT NULL ,
	participationStatus varchar(50) NULL
);

CREATE TABLE calendarJournalCategory (
	journalId int NOT NULL ,
	categoryId varchar(50) NOT NULL
);

CREATE TABLE days (
	day varchar(50) NOT NULL
);

CREATE TABLE calendarToDo (
	id int NOT NULL ,
	name varchar(2000) NOT NULL ,
	description varchar(4000) NULL ,
	delegatorId varchar(100) NOT NULL ,
	startDay varchar(50) NULL ,
	endDay varchar(50) NULL ,
	startHour varchar(50) NULL ,
	endHour varchar(50) NULL ,
	classification varchar(20) NULL ,
	priority int NULL ,
	lastModification varchar(50) NULL ,
	percentCompleted int NULL ,
	completedDay varchar(20) NULL ,
	duration int NULL ,
	componentId varchar(100) NULL ,
	spaceId varchar(100) NULL ,
	externalId varchar(100) NULL
);


CREATE TABLE calendarToDoAttendee (
	todoId int NOT NULL ,
	userId varchar(100) NOT NULL ,
	participationStatus varchar(50) NULL
);

CREATE TABLE ST_FormDesigner_FormDesign (
	ID int NOT NULL ,
	REFIDFORM int NOT NULL ,
	COMPONENTID varchar(100) NOT NULL ,
	NAME varchar(1000) NOT NULL ,
	DESCRIPTION varchar(2000) NOT NULL ,
	CREATIONDATE varchar(10) NOT NULL ,
	AUTHOR int  NOT NULL
);

CREATE TABLE ST_FormDesigner_Connectors (
	ID int NOT NULL ,
	NAME varchar(1000) NOT NULL ,
	DESCRIPTION varchar(2000) NOT NULL,
	DRIVER varchar(1000) NOT NULL,
	URL varchar(1000) NOT NULL,
	LOGIN varchar(1000) NOT NULL,
	PASSWD varchar(1000) NULL,
	SQLQUERY varchar(5000) NOT NULL,
	TYPE varchar(50) NOT NULL
);

CREATE TABLE ST_FormEditor_FormEdited (
	ID int NOT NULL ,
	FORMID int NOT NULL ,
	USERID int NOT NULL,
	CREATEDATE varchar(10) NOT NULL,
	MODIFYDATE varchar(10) NOT NULL
);

CREATE TABLE ST_FormEditor_FormEditedData (
	ID int NOT NULL ,
	FORMEDITEDID int NOT NULL ,
	EDITEDKEY varchar(50) NOT NULL,
	EDITEDVALUE varchar(500) NOT NULL
);

CREATE TABLE sb_agenda_import_settings
(
  userid int NOT NULL,
  hostname varchar(500) NOT NULL,
  synchrotype int NOT NULL,
  synchrodelay int NOT NULL,
  url varchar(500) NULL,
  remotelogin varchar(200) NULL,
  remotepwd varchar(200) NULL,
  charset varchar(20) NULL
);