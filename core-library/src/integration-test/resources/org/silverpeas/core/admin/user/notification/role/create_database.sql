CREATE TABLE ST_AccessLevel
(
    id   CHAR(1)      NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
    CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE ST_User
(
    id                            INT                   NOT NULL,
    domainId                      INT                   NOT NULL,
    specificId                    VARCHAR(500)          NOT NULL,
    firstName                     VARCHAR(100),
    lastName                      VARCHAR(100)          NOT NULL,
    email                         VARCHAR(100),
    login                         VARCHAR(50)           NOT NULL,
    loginMail                     VARCHAR(100),
    accessLevel                   CHAR(1) DEFAULT 'U'   NOT NULL,
    loginquestion                 VARCHAR(200),
    loginanswer                   VARCHAR(200),
    creationDate                  TIMESTAMP,
    saveDate                      TIMESTAMP,
    version                       INT     DEFAULT 0     NOT NULL,
    tosAcceptanceDate             TIMESTAMP,
    lastLoginDate                 TIMESTAMP,
    nbSuccessfulLoginAttempts     INT     DEFAULT 0     NOT NULL,
    lastLoginCredentialUpdateDate TIMESTAMP,
    expirationDate                TIMESTAMP,
    state                         VARCHAR(30)           NOT NULL,
    stateSaveDate                 TIMESTAMP             NOT NULL,
    notifManualReceiverLimit      INT,
    sensitiveData                 BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT PK_User PRIMARY KEY (id),
    CONSTRAINT UN_User_1 UNIQUE (specificId, domainId),
    CONSTRAINT UN_User_2 UNIQUE (login, domainId),
    CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id)
);

CREATE TABLE ST_Group
(
    id            int          NOT NULL,
    domainId      int          NOT NULL,
    specificId    varchar(500) NOT NULL,
    superGroupId  int,
    name          varchar(100) NOT NULL,
    description   varchar(400),
    synchroRule   varchar(100),
    creationDate  timestamp,
    saveDate      timestamp,
    state         varchar(30)  NOT NULL,
    stateSaveDate timestamp    NOT NULL,
    CONSTRAINT PK_Group PRIMARY KEY (id),
    CONSTRAINT UN_Group_1 UNIQUE (specificId, domainId),
    CONSTRAINT UN_Group_2 UNIQUE (superGroupId, name, domainId),
    CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group (id)
);

CREATE TABLE ST_Group_User_Rel
(
    groupId int NOT NULL,
    userId  int NOT NULL,
    CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId),
    CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group (id),
    CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE ST_Space
(
    id                   INT             NOT NULL,
    domainFatherId       INT,
    name                 VARCHAR(100)    NOT NULL,
    description          VARCHAR(400),
    createdBy            INT,
    firstPageType        INT             NOT NULL,
    firstPageExtraParam  VARCHAR(400),
    orderNum             INT DEFAULT (0) NOT NULL,
    createTime           VARCHAR(20),
    updateTime           VARCHAR(20),
    removeTime           VARCHAR(20),
    spaceStatus          CHAR(1),
    updatedBy            INT,
    removedBy            INT,
    lang                 CHAR(2),
    isInheritanceBlocked INT DEFAULT (0) NOT NULL,
    look                 VARCHAR(50),
    displaySpaceFirst    SMALLINT,
    isPersonal           SMALLINT,
    CONSTRAINT PK_Space PRIMARY KEY (id),
    CONSTRAINT UN_Space_1 UNIQUE (domainFatherId, name),
    CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User (id),
    CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space (id)
);

CREATE TABLE ST_SpaceI18N
(
    id          INT          NOT NULL,
    spaceId     INT          NOT NULL,
    lang        CHAR(2)      NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(400)
);

CREATE TABLE ST_ComponentInstance
(
    id                   INT             NOT NULL,
    spaceId              INT             NOT NULL,
    name                 VARCHAR(100)    NOT NULL,
    componentName        VARCHAR(100)    NOT NULL,
    description          VARCHAR(400),
    createdBy            INT,
    orderNum             INT DEFAULT (0) NOT NULL,
    createTime           VARCHAR(20),
    updateTime           VARCHAR(20),
    removeTime           VARCHAR(20),
    componentStatus      CHAR(1),
    updatedBy            INT,
    removedBy            INT,
    isPublic             INT DEFAULT (0) NOT NULL,
    isHidden             INT DEFAULT (0) NOT NULL,
    lang                 CHAR(2),
    isInheritanceBlocked INT DEFAULT (0) NOT NULL,
    CONSTRAINT PK_ComponentInstance PRIMARY KEY (id),
    CONSTRAINT UN_ComponentInstance_1 UNIQUE (spaceId, name),
    CONSTRAINT FK_ComponentInstance_1 FOREIGN KEY (spaceId) REFERENCES ST_Space (id),
    CONSTRAINT FK_ComponentInstance_2 FOREIGN KEY (createdBy) REFERENCES ST_User (id)
);

CREATE TABLE ST_ComponentInstanceI18N
(
    id          INT          NOT NULL,
    componentId INT          NOT NULL,
    lang        CHAR(2)      NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(400)
);

CREATE TABLE ST_Instance_Data
(
    id          INT          NOT NULL,
    componentId INT          NOT NULL,
    name        VARCHAR(100) NOT NULL,
    label       VARCHAR(100) NOT NULL,
    value       VARCHAR(400),
    CONSTRAINT PK_Instance_Data PRIMARY KEY (id),
    CONSTRAINT UN_Instance_Data_1 UNIQUE (componentId, name),
    CONSTRAINT FK_Instance_Data_1 FOREIGN KEY (componentId) REFERENCES ST_ComponentInstance (id)
);

CREATE TABLE SB_ContentManager_Instance
(
    instanceId    int          NOT NULL,
    componentId   varchar(100) NOT NULL,
    containerType varchar(100) NOT NULL,
    contentType   varchar(100) NOT NULL
);

CREATE TABLE ST_SpaceUserRole
(
    id          INT             NOT NULL,
    spaceId     INT             NOT NULL,
    name        VARCHAR(100)    NULL,
    roleName    VARCHAR(100)    NOT NULL,
    description VARCHAR(400),
    isInherited INT DEFAULT (0) NOT NULL,
    CONSTRAINT PK_SpaceUserRole PRIMARY KEY (id),
    CONSTRAINT UN_SpaceUserRole_1 UNIQUE (spaceId, roleName, isInherited),
    CONSTRAINT FK_SpaceUserRole_1 FOREIGN KEY (spaceId) REFERENCES ST_Space (id)
);

CREATE TABLE ST_SpaceUserRole_User_Rel
(
    spaceUserRoleId INT NOT NULL,
    userId          INT NOT NULL,
    CONSTRAINT PK_SpaceUserRole_User_Rel PRIMARY KEY (spaceUserRoleId, userId),
    CONSTRAINT FK_SpaceUserRole_User_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole (id),
    CONSTRAINT FK_SpaceUserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE ST_SpaceUserRole_Group_Rel
(
    spaceUserRoleId INT NOT NULL,
    groupId         INT NOT NULL,
    CONSTRAINT PK_SpaceUserRole_Group_Rel PRIMARY KEY (spaceUserRoleId, groupId),
    CONSTRAINT FK_SpaceUserRole_Group_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole (id),
    CONSTRAINT FK_SpaceUserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group (id)
);

CREATE TABLE ST_UserRole
(
    id          INT             NOT NULL,
    instanceId  INT             NOT NULL,
    name        VARCHAR(100)    NULL,
    roleName    VARCHAR(100)    NOT NULL,
    description VARCHAR(400),
    isInherited INT DEFAULT (0) NOT NULL,
    objectId    INT,
    objectType  VARCHAR(50),
    CONSTRAINT PK_UserRole PRIMARY KEY (id),
    CONSTRAINT UN_UserRole_1 UNIQUE (instanceId, roleName, isInherited, objectId),
    CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance (id)
);

CREATE TABLE ST_UserRole_User_Rel
(
    userRoleId INT NOT NULL,
    userId     INT NOT NULL,
    CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId),
    CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole (id),
    CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE ST_UserRole_Group_Rel
(
    userRoleId INT NOT NULL,
    groupId    INT NOT NULL,
    CONSTRAINT PK_UserRole_Group_Rel PRIMARY KEY (userRoleId, groupId),
    CONSTRAINT FK_UserRole_Group_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole (id),
    CONSTRAINT FK_UserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group (id)
);

/*
 * The SQL tables of a given component used in tests
 */

/* the resources managed by the instances of the component */
CREATE TABLE SC_MyComponent_Resources
(
    id             BIGINT        NOT NULL,
    instanceId     VARCHAR(50)   NOT NULL,
    name           VARCHAR(128)  NOT NULL,
    description    VARCHAR       NOT NULL DEFAULT '',
    creationDate   DATETIME      NOT NULL,
    updateDate     DATETIME      NOT NULL,
    creator        VARCHAR(50)   NOT NULL,
    updater        VARCHAR(50)   NOT NULL ,
    validator      VARCHAR(50),
    validationDate DATETIME,
    CONSTRAINT PK_MyComponent_Resources PRIMARY KEY (id)
);

/* the list of the validators of a given resource of a component instance */
CREATE TABLE SC_MyComponent_Validators
(
    resourceId  VARCHAR(50) NOT NULL,
    validatorId VARCHAR(50) NOT NULL,
    CONSTRAINT PK_MyComponent_Validator PRIMARY KEY (resourceId, validatorId),
    CONSTRAINT FK_MyComponent_Resources FOREIGN KEY (resourceId) REFERENCES SC_MyComponent_Resources(id)
);
