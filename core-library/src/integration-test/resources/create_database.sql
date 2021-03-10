CREATE TABLE ST_AccessLevel
(
    id   char(1)       NOT NULL,
    name varchar(100)  NOT NULL
);

CREATE TABLE ST_User
(
    id                            INT                  NOT NULL,
    domainId                      INT                  NOT NULL,
    specificId                    VARCHAR(500)         NOT NULL,
    firstName                     VARCHAR(100),
    lastName                      VARCHAR(100)         NOT NULL,
    email                         VARCHAR(100),
    login                         VARCHAR(100)         NOT NULL,
    loginMail                     VARCHAR(100),
    accessLevel                   CHAR(1) DEFAULT 'U'  NOT NULL,
    loginquestion                 VARCHAR(200),
    loginanswer                   VARCHAR(200),
    creationDate                  TIMESTAMP,
    saveDate                      TIMESTAMP,
    version                       INT DEFAULT 0        NOT NULL,
    tosAcceptanceDate             TIMESTAMP,
    lastLoginDate                 TIMESTAMP,
    nbSuccessfulLoginAttempts     INT DEFAULT 0        NOT NULL,
    lastLoginCredentialUpdateDate TIMESTAMP,
    expirationDate                TIMESTAMP,
    state                         VARCHAR(30)          NOT NULL,
    stateSaveDate                 TIMESTAMP            NOT NULL,
    notifManualReceiverLimit      INT
);

CREATE TABLE ST_Group
(
    id              int           NOT NULL,
    domainId        int           NOT NULL,
    specificId      varchar(500)  NOT NULL,
    superGroupId    int,
    name            varchar(100)  NOT NULL,
    description     varchar(400),
    synchroRule     varchar(2000)
);

CREATE TABLE ST_Group_User_Rel
(
    groupId int NOT NULL,
    userId  int NOT NULL
);

CREATE TABLE ST_Space
(
    id					int           NOT NULL,
    domainFatherId		int,
    name				varchar(100)  NOT NULL,
    description			varchar(400),
    createdBy			int,
    firstPageType		int           NOT NULL,
    firstPageExtraParam	varchar(400),
    orderNum 			int DEFAULT (0) NOT NULL,
    createTime 			varchar(20),
    updateTime 			varchar(20),
    removeTime 			varchar(20),
    spaceStatus 		char(1),
    updatedBy 			int,
    removedBy 			int,
    lang			char(2),
    isInheritanceBlocked	int	      default(0) NOT NULL,
    look			varchar(50),
    displaySpaceFirst		smallint,
    isPersonal			smallint
);

CREATE TABLE ST_SpaceI18N
(
    id			int		NOT NULL,
    spaceId		int		NOT NULL,
    lang		char(2)		NOT NULL,
    name		varchar(100)	NOT NULL,
    description		varchar(400)
);

CREATE TABLE ST_ComponentInstance
(
    id            	int           NOT NULL,
    spaceId       	int           NOT NULL,
    name          	varchar(100)  NOT NULL,
    componentName 	varchar(100)  NOT NULL,
    description   	varchar(400),
    createdBy     	int,
    orderNum 		int DEFAULT (0) NOT NULL,
    createTime 		varchar(20),
    updateTime 		varchar(20),
    removeTime 		varchar(20),
    componentStatus char(1),
    updatedBy 		int,
    removedBy 		int,
    isPublic		int	DEFAULT(0)	NOT NULL,
    isHidden		int	DEFAULT(0)	NOT NULL,
    lang		char(2),
    isInheritanceBlocked	int	default(0) NOT NULL
);

CREATE TABLE ST_ComponentInstanceI18N
(
    id			int		NOT NULL,
    componentId		int		NOT NULL,
    lang		char(2)		NOT NULL,
    name		varchar(100)	NOT NULL,
    description		varchar(400)
);

CREATE TABLE ST_Instance_Data
(
    id            int           NOT NULL,
    componentId   int           NOT NULL,
    name          varchar(100)  NOT NULL,
    label	  varchar(100)  NOT NULL,
    value	  varchar(400)
);

CREATE TABLE ST_UserRole
(
    id            int           NOT NULL,
    instanceId    int           NOT NULL,
    name          varchar(100)  NULL,
    roleName      varchar(100)  NOT NULL,
    description   varchar(400),
    isInherited	  int	        default(0) NOT NULL,
    objectId	  int,
    objectType	  varchar(50)
);

CREATE TABLE ST_UserRole_User_Rel
(
    userRoleId   int NOT NULL,
    userId       int NOT NULL
);

CREATE TABLE ST_UserRole_Group_Rel
(
    userRoleId   int NOT NULL,
    groupId      int NOT NULL
);

CREATE TABLE ST_SpaceUserRole
(
    id            int           NOT NULL,
    spaceId	  int           NOT NULL,
    name          varchar(100)  NULL,
    roleName      varchar(100)  NOT NULL,
    description   varchar(400),
    isInherited	  int	        default(0) NOT NULL
);

CREATE TABLE ST_SpaceUserRole_User_Rel
(
    spaceUserRoleId   int NOT NULL,
    userId            int NOT NULL
);

CREATE TABLE ST_SpaceUserRole_Group_Rel
(
    spaceUserRoleId   int NOT NULL,
    groupId           int NOT NULL
);

CREATE TABLE DomainSP_Group (
                                id 		int NOT NULL,
                                superGroupId int NULL ,
                                name		varchar (100) NOT NULL ,
                                description 	varchar (400) NULL
);

CREATE TABLE DomainSP_User (
                               id		int NOT NULL,
                               firstName	varchar (100) NULL ,
                               lastName	varchar (100) NOT NULL ,
                               phone		varchar (20) NULL ,
                               homePhone	varchar (20) NULL ,
                               cellPhone	varchar (20) NULL ,
                               fax		varchar (20) NULL ,
                               address		varchar (500) NULL ,
                               title		varchar (100) NULL ,
                               company		varchar (100) NULL ,
                               position	varchar (100) NULL ,
                               boss		varchar (100) NULL ,
                               login		varchar (50) NOT NULL ,
                               password	varchar (123) NULL ,
                               passwordValid	char (1) DEFAULT ('Y') NOT NULL ,
                               loginMail	varchar (100) NULL ,
                               email		varchar (100) NULL
);

CREATE TABLE DomainSP_Group_User_Rel (
                                         groupId 	int NOT NULL ,
                                         userId	int NOT NULL
);

CREATE TABLE ST_Domain (
                           id			int NOT NULL ,
                           name			varchar (100) NOT NULL ,
                           description		varchar (400) NULL ,
                           propFileName		varchar (100) NOT NULL ,
                           className		varchar (100) NOT NULL ,
                           authenticationServer	varchar (100) NOT NULL ,
                           theTimeStamp            varchar (100) DEFAULT('0') NOT NULL ,
                           silverpeasServerURL     varchar (400) NULL
);

CREATE TABLE ST_KeyStore (
                             userKey		decimal(18, 0)	NOT NULL ,
                             login		varchar(100)	NOT NULL ,
                             domainId	int		NOT NULL
);


CREATE TABLE ST_LongText (
                             id int NOT NULL ,
                             orderNum int NOT NULL ,
                             bodyContent varchar(2000) NOT NULL
);

CREATE TABLE ST_GroupUserRole
(
    id            int           NOT NULL,
    groupId	  int           NOT NULL,
    roleName      varchar(100)  NOT NULL
);

CREATE TABLE ST_GroupUserRole_User_Rel
(
    groupUserRoleId   int NOT NULL,
    userId            int NOT NULL
);

CREATE TABLE ST_GroupUserRole_Group_Rel
(
    groupUserRoleId   int NOT NULL,
    groupId           int NOT NULL
);

CREATE TABLE st_instance_modelused
(
    instanceId		varchar(50)     NOT NULL,
    modelId			varchar(50)     NOT NULL,
    objectId		varchar(50)	DEFAULT('0') NOT NULL
)
;

CREATE TABLE ST_UserFavoriteSpaces
(
    id          INT   NOT NULL,
    userid      INT   NOT NULL,
    spaceid     INT   NOT NULL
);

CREATE TABLE UniqueId (
                          maxId BIGINT NOT NULL,
                          tableName varchar(100) NOT NULL
);

CREATE TABLE Personalization (
                                 id varchar(100) NOT NULL ,
                                 languages varchar(100) NULL,
                                 zoneId varchar(100) NULL,
                                 look varchar(50) NULL,
                                 personalWSpace varchar(50) NULL,
                                 thesaurusStatus int NOT NULL,
                                 dragAndDropStatus int DEFAULT 1,
                                 webdavEditingStatus int DEFAULT 0,
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
                           creationDate       TIMESTAMP    NOT NULL
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
                       partId int default 1 NOT NULL
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
                                            SQLQUERY varchar(4000) NOT NULL,
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

ALTER TABLE UniqueId  ADD
	CONSTRAINT PK_UniqueId PRIMARY KEY
	(
		tableName
	);

ALTER TABLE Personalization  ADD
    CONSTRAINT PK_Personalization PRIMARY KEY
        (
         id
            );

ALTER TABLE readingControl  ADD
    CONSTRAINT PK_readingControl PRIMARY KEY
        (
         pubId, actorId, space, componentName
            );

ALTER TABLE model_contact  ADD
    CONSTRAINT PK_model_contact PRIMARY KEY
        (
         id
            );

ALTER TABLE model  ADD
    CONSTRAINT PK_model PRIMARY KEY
        (
         id
            );

ALTER TABLE calendarJournal  ADD
    CONSTRAINT PK_CalendarJournal PRIMARY KEY
        (
         id
            );

ALTER TABLE calendarCategory  ADD
    CONSTRAINT PK_CalendarCategory PRIMARY KEY
        (
         categoryId
            );

ALTER TABLE calendarJournalAttendee  ADD
    CONSTRAINT PK_calendarJournalAttendee PRIMARY KEY
        (
         journalId, userId
            );


ALTER TABLE calendarJournalCategory  ADD
    CONSTRAINT PK_calendarJournalCategory PRIMARY KEY
        (
         journalId, categoryId
            );

ALTER TABLE calendarToDo  ADD
    CONSTRAINT PK_calendarToDo PRIMARY KEY
        (
         id
            );

ALTER TABLE calendarToDoAttendee  ADD
    CONSTRAINT PK_calendarToDoAttendee PRIMARY KEY
        (
         todoId, userId
            );

ALTER TABLE ST_FormDesigner_FormDesign  ADD
    CONSTRAINT PK_ST_FormDesigner_FormDesign PRIMARY KEY
        (
         ID
            );

ALTER TABLE ST_FormDesigner_Connectors  ADD
    CONSTRAINT PK_ST_FormDesigner_Connectors PRIMARY KEY
        (
         ID
            );

ALTER TABLE ST_FormEditor_FormEdited  ADD
    CONSTRAINT PK_ST_FormEditor_FormEdited PRIMARY KEY
        (
         ID
            );

ALTER TABLE ST_FormEditor_FormEditedData  ADD
    CONSTRAINT PK_ST_FormEditor_FED PRIMARY KEY
        (
         ID
            );

ALTER TABLE sb_agenda_import_settings ADD
    CONSTRAINT PK_sb_agenda_import_settings_SET PRIMARY KEY
        (
         userid
            );

ALTER TABLE ST_AccessLevel  ADD CONSTRAINT PK_AccessLevel PRIMARY KEY (id);
ALTER TABLE ST_AccessLevel ADD CONSTRAINT UN_AccessLevel_1 UNIQUE (name);

ALTER TABLE ST_User ADD CONSTRAINT PK_User PRIMARY KEY (id);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_1 UNIQUE(specificId, domainId);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_2 UNIQUE(login, domainId);
ALTER TABLE ST_User ADD CONSTRAINT FK_User_1 FOREIGN KEY(accessLevel) REFERENCES ST_AccessLevel(id);

ALTER TABLE ST_Group  ADD CONSTRAINT PK_Group PRIMARY KEY (id);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_1 UNIQUE(specificId, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_2 UNIQUE(superGroupId, name, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group(id);

ALTER TABLE ST_Group_User_Rel  ADD CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group(id);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_Space  ADD CONSTRAINT PK_Space PRIMARY KEY (id);
ALTER TABLE ST_Space ADD CONSTRAINT UN_Space_1 UNIQUE(domainFatherId, name);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User(id);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space(id);

ALTER TABLE ST_ComponentInstance  ADD CONSTRAINT PK_ComponentInstance PRIMARY KEY (id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT UN_ComponentInstance_1 UNIQUE(spaceId, name);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_2 FOREIGN KEY (createdBy) REFERENCES ST_User(id);

ALTER TABLE ST_Instance_Data  ADD CONSTRAINT PK_Instance_Data PRIMARY KEY (id);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT UN_Instance_Data_1 UNIQUE(componentId, name);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT FK_Instance_Data_1 FOREIGN KEY (componentId) REFERENCES ST_ComponentInstance(id);

ALTER TABLE ST_UserRole  ADD CONSTRAINT PK_UserRole PRIMARY KEY (id);
ALTER TABLE ST_UserRole ADD CONSTRAINT UN_UserRole_1 UNIQUE(instanceId, roleName, isInherited, objectId);
ALTER TABLE ST_UserRole ADD CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance(id);

ALTER TABLE ST_UserRole_User_Rel  ADD CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_UserRole_Group_Rel  ADD CONSTRAINT PK_UserRole_Group_Rel PRIMARY KEY (userRoleId, groupId);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id);

ALTER TABLE ST_SpaceUserRole  ADD CONSTRAINT PK_SpaceUserRole PRIMARY KEY (id);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT UN_SpaceUserRole_1 UNIQUE(spaceId, roleName, isInherited);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT FK_SpaceUserRole_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id);

ALTER TABLE ST_SpaceUserRole_User_Rel  ADD CONSTRAINT PK_SpaceUserRole_User_Rel PRIMARY KEY (spaceUserRoleId, userId);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_SpaceUserRole_Group_Rel  ADD CONSTRAINT PK_SpaceUserRole_Group_Rel PRIMARY KEY (spaceUserRoleId, groupId);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id);

ALTER TABLE DomainSP_Group  ADD CONSTRAINT PK_DomainSP_Group PRIMARY KEY (id);
ALTER TABLE DomainSP_Group ADD CONSTRAINT UN_DomainSP_Group_1 UNIQUE(superGroupId, name);
ALTER TABLE DomainSP_Group ADD CONSTRAINT FK_DomainSP_Group_1 FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group(id);

ALTER TABLE DomainSP_User  ADD CONSTRAINT PK_DomainSP_User PRIMARY KEY (id);
ALTER TABLE DomainSP_User ADD CONSTRAINT UN_DomainSP_User_1 UNIQUE(login);

ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES DomainSP_Group(id);
ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES DomainSP_User(id);

ALTER TABLE ST_Domain  ADD CONSTRAINT PK_ST_Domain PRIMARY KEY (id);

ALTER TABLE DomainSP_Group_User_Rel  ADD CONSTRAINT PK_DomainSP_Group_User_Rel PRIMARY KEY (groupId,userId);

ALTER TABLE ST_LongText ADD CONSTRAINT PK_ST_LongText PRIMARY KEY (id,orderNum);

ALTER TABLE st_instance_modelused ADD
    CONSTRAINT PK_st_instance_modelused PRIMARY KEY
        (
         instanceId,
         modelId,
         objectId
            )
;

ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT PK_UserFavoriteSpaces PRIMARY KEY (id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_1 FOREIGN KEY (userid) REFERENCES ST_User(id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_2 FOREIGN KEY (spaceid) REFERENCES ST_Space(id);

insert into ST_AccessLevel(id, name) values ('U', 'User');
insert into ST_AccessLevel(id, name) values ('A', 'Administrator');
insert into ST_AccessLevel(id, name) values ('G', 'Guest');
insert into ST_AccessLevel(id, name) values ('R', 'Removed');
insert into ST_AccessLevel(id, name) values ('K', 'KMManager');
insert into ST_AccessLevel(id, name) values ('D', 'DomainManager');

INSERT INTO ST_User (id, specificId, domainId, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (0, '0', 0, '${SILVERPEAS_ADMIN_NAME}', '${SILVERPEAS_ADMIN_EMAIL}', '${SILVERPEAS_ADMIN_LOGIN}', 'A', 'VALID', CURRENT_TIMESTAMP);

insert into DomainSP_User(id, lastName, login, password, email)
values             (0, '${SILVERPEAS_ADMIN_NAME}', '${SILVERPEAS_ADMIN_LOGIN}', '${SILVERPEAS_ADMIN_PASSWORD}', '${SILVERPEAS_ADMIN_EMAIL}');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-', '0', '');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (0, 'domainSilverpeas', 'default domain for Silverpeas', 'org.silverpeas.domains.domainSP', 'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', '0', '${SERVER_URL}');

insert into calendarCategory(categoryId, name) values (1, 'Réunion')
;
insert into calendarCategory(categoryId, name) values (2, 'Déplacement')
;
insert into calendarCategory(categoryId, name) values (3, 'Vacances')
;
insert into calendarCategory(categoryId, name) values (4, 'Personnel')
;
insert into calendarCategory(categoryId, name) values (5, 'Brain Storming')
;
insert into calendarCategory(categoryId, name) values (6, 'Formation')
;

insert into days(day) values ('01')
;
insert into days(day) values ('02')
;
insert into days(day) values ('03')
;
insert into days(day) values ('04')
;
insert into days(day) values ('05')
;
insert into days(day) values ('06')
;
insert into days(day) values ('07')
;
insert into days(day) values ('08')
;
insert into days(day) values ('09')
;
insert into days(day) values ('10')
;
insert into days(day) values ('11')
;
insert into days(day) values ('12')
;
insert into days(day) values ('13')
;
insert into days(day) values ('14')
;
insert into days(day) values ('15')
;
insert into days(day) values ('16')
;
insert into days(day) values ('17')
;
insert into days(day) values ('18')
;
insert into days(day) values ('19')
;
insert into days(day) values ('20')
;
insert into days(day) values ('21')
;
insert into days(day) values ('22')
;
insert into days(day) values ('23')
;
insert into days(day) values ('24')
;
insert into days(day) values ('25')
;
insert into days(day) values ('26')
;
insert into days(day) values ('27')
;
insert into days(day) values ('28')
;
insert into days(day) values ('29')
;
insert into days(day) values ('30')
;
insert into days(day) values ('31')
;

insert into ST_FormDesigner_Connectors
(ID, NAME, DESCRIPTION, DRIVER, URL, LOGIN, PASSWD, SQLQUERY, TYPE) values
(0, '________', ' ', ' ', ' ', ' ', ' ', ' ', ' ')
;


CREATE TABLE SB_Cal_Calendar (
                                 id             VARCHAR(40)   NOT NULL,
                                 instanceId     VARCHAR(30)   NOT NULL,
                                 title          VARCHAR(255)  NOT NULL,
                                 zoneId         VARCHAR(40)   NOT NULL,
                                 externalUrl    VARCHAR(250),
                                 synchroDate    TIMESTAMP,
                                 createDate     TIMESTAMP     NOT NULL,
                                 createdBy      VARCHAR(40)   NOT NULL,
                                 lastUpdateDate TIMESTAMP     NOT NULL,
                                 lastUpdatedBy  VARCHAR(40)   NOT NULL,
                                 version        INT8          NOT NULL,
                                 CONSTRAINT PK_CALENDAR PRIMARY KEY (id)
);

CREATE TABLE SB_Cal_Recurrence (
                                   id                   VARCHAR(40)  NOT NULL,
                                   recur_periodInterval INT          NOT NULL,
                                   recur_periodUnit     VARCHAR(5)   NOT NULL,
                                   recur_count          INT          DEFAULT 0,
                                   recur_endDate        TIMESTAMP,
                                   CONSTRAINT PK_RECURRENCE PRIMARY KEY (id)
);

CREATE TABLE SB_Cal_Recurrence_DayOfWeek (
                                             recurrenceId    VARCHAR(40) NOT NULL,
                                             recur_nth       INT         NOT NULL,
                                             recur_dayOfWeek INT         NOT NULL,
                                             CONSTRAINT FK_Recurrence_DayOfWeek FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE SB_Cal_Recurrence_Exception (
                                             recurrenceId        VARCHAR(40) NOT NULL,
                                             recur_exceptionDate TIMESTAMP   NOT NULL,
                                             CONSTRAINT FK_Recurrence_Exception FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE SB_Cal_Components (
                                   id             VARCHAR(40)   NOT NULL,
                                   calendarId     VARCHAR(40)   NOT NULL,
                                   startDate      TIMESTAMP     NOT NULL,
                                   endDate        TIMESTAMP     NOT NULL,
                                   inDays         BOOLEAN       NOT NULL,
                                   title          VARCHAR(255)  NOT NULL,
                                   description    VARCHAR(2000) NOT NULL,
                                   location       VARCHAR(255)  NULL,
                                   attributes     VARCHAR(40)   NULL,
                                   priority       INT           NOT NULL,
                                   sequence       INT8          NOT NULL DEFAULT 0,
                                   createDate     TIMESTAMP     NOT NULL,
                                   createdBy      VARCHAR(40)   NOT NULL,
                                   lastUpdateDate TIMESTAMP     NOT NULL,
                                   lastUpdatedBy  VARCHAR(40)   NOT NULL,
                                   version        INT8          NOT NULL,
                                   CONSTRAINT PK_CalComponent PRIMARY KEY (id),
                                   CONSTRAINT FK_Calendar     FOREIGN KEY (calendarId) REFERENCES SB_Cal_Calendar(id)
);

CREATE TABLE SB_Cal_Event (
                              id             VARCHAR(40)   NOT NULL,
                              externalId     VARCHAR(255)  NULL,
                              synchroDate    TIMESTAMP,
                              componentId    VARCHAR(40)   NOT NULL,
                              visibility     VARCHAR(50)   NOT NULL,
                              recurrenceId   VARCHAR(40)   NULL,
                              CONSTRAINT PK_Event PRIMARY KEY (id),
                              CONSTRAINT FK_Event_Component  FOREIGN KEY (componentId)  REFERENCES SB_Cal_Components(id),
                              CONSTRAINT FK_Event_Recurrence FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE SB_Cal_Occurrences (
                                    id             VARCHAR(60)   NOT NULL,
                                    eventId        VARCHAR(40)   NOT NULL,
                                    componentId    VARCHAR(40)   NOT NULL,
                                    CONSTRAINT PK_Occurrence           PRIMARY KEY (id),
                                    CONSTRAINT FK_Occurrence_Event     FOREIGN KEY (eventId)     REFERENCES SB_Cal_Event,
                                    CONSTRAINT FK_Occurrence_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components(id)
);

CREATE TABLE SB_Cal_Attributes (
                                   id         VARCHAR(40)  NOT NULL,
                                   name       VARCHAR(255) NOT NULL,
                                   value      VARCHAR(255) NOT NULL,
                                   CONSTRAINT PK_Attributes PRIMARY KEY (id, name)
);

CREATE TABLE SB_Cal_Categories (
                                   id       VARCHAR(40) NOT NULL,
                                   category VARCHAR(255) NOT NULL,
                                   CONSTRAINT Pk_Categories PRIMARY KEY (id, category)
);

CREATE TABLE SB_Cal_Attendees (
                                  id                VARCHAR(40) NOT NULL,
                                  attendeeId        VARCHAR(40) NOT NULL,
                                  componentId       VARCHAR(40) NOT NULL,
                                  type              INT         NOT NULL,
                                  participation     VARCHAR(12) NOT NULL DEFAULT 'AWAITING',
                                  presence          VARCHAR(12) NOT NULL DEFAULT 'REQUIRED',
                                  delegate          VARCHAR(40) NULL,
                                  createDate        TIMESTAMP   NOT NULL,
                                  createdBy         VARCHAR(40) NOT NULL,
                                  lastUpdateDate    TIMESTAMP   NOT NULL,
                                  lastUpdatedBy     VARCHAR(40) NOT NULL,
                                  version           INT8        NOT NULL,
                                  CONSTRAINT PK_Attendee PRIMARY KEY (id),
                                  CONSTRAINT FK_Attendee_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components(id),
                                  CONSTRAINT FK_Delegate FOREIGN KEY (delegate) REFERENCES SB_Cal_Attendees(id)
);


CREATE TABLE SB_ClassifyEngine_Classify
(
    PositionId int NOT NULL ,
    ObjectId int NOT NULL ,
    Axis0	varchar (150),
    Axis1	varchar (150),
    Axis2	varchar (150),
    Axis3	varchar (150),
    Axis4	varchar (150),
    Axis5	varchar (150),
    Axis6	varchar (150),
    Axis7	varchar (150),
    Axis8	varchar (150),
    Axis9	varchar (150),
    Axis10	varchar (150),
    Axis11	varchar (150),
    Axis12	varchar (150),
    Axis13	varchar (150),
    Axis14	varchar (150),
    Axis15	varchar (150),
    Axis16	varchar (150),
    Axis17	varchar (150),
    Axis18	varchar (150),
    Axis19	varchar (150),
    Axis20	varchar (150),
    Axis21	varchar (150),
    Axis22	varchar (150),
    Axis23	varchar (150),
    Axis24	varchar (150),
    Axis25	varchar (150),
    Axis26	varchar (150),
    Axis27	varchar (150),
    Axis28	varchar (150),
    Axis29	varchar (150),
    Axis30	varchar (150),
    Axis31	varchar (150),
    Axis32	varchar (150),
    Axis33	varchar (150),
    Axis34	varchar (150),
    Axis35	varchar (150),
    Axis36	varchar (150),
    Axis37	varchar (150),
    Axis38	varchar (150),
    Axis39	varchar (150),
    Axis40	varchar (150),
    Axis41	varchar (150),
    Axis42	varchar (150),
    Axis43	varchar (150),
    Axis44	varchar (150),
    Axis45	varchar (150),
    Axis46	varchar (150),
    Axis47	varchar (150),
    Axis48	varchar (150),
    Axis49	varchar (150)
);

ALTER TABLE SB_ClassifyEngine_Classify ADD
    CONSTRAINT PK_ClassifyEngine_Classify PRIMARY KEY
        (
         PositionId
            )
;

INSERT INTO SB_ClassifyEngine_Classify(PositionId, ObjectId, Axis0,  Axis1,  Axis2,  Axis3,  Axis4,  Axis5,  Axis6,  Axis7,  Axis8,  Axis9,
                                       Axis10, Axis11, Axis12, Axis13, Axis14, Axis15, Axis16, Axis17, Axis18, Axis19,
                                       Axis20, Axis21, Axis22, Axis23, Axis24, Axis25, Axis26, Axis27, Axis28, Axis29,
                                       Axis30, Axis31, Axis32, Axis33, Axis34, Axis35, Axis36, Axis37, Axis38, Axis39,
                                       Axis40, Axis41, Axis42, Axis43, Axis44, Axis45, Axis46, Axis47, Axis48, Axis49)
Values(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1);


CREATE TABLE SB_Comment_Comment
(
    commentId int not null,
    commentOwnerId int not null,
    commentCreationDate char (10) not null,
    commentModificationDate char (10),
    commentComment varchar (2000) not null,
    instanceId varchar (50) not null,
    resourceType varchar (50) not null,
    resourceId varchar (50) not null
);

ALTER TABLE SB_Comment_Comment ADD
    CONSTRAINT PK_Comment_Comment PRIMARY KEY
        (
         commentid
            );

/* contact */

