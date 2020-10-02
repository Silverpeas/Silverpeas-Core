CREATE TABLE IF NOT EXISTS UniqueId
(
    maxId     int          NOT NULL,
    tableName varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS DomainSP_Group
(
    id           INT          NOT NULL,
    superGroupId INT          NULL,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(400) NULL,
    CONSTRAINT PK_DomainSP_Group PRIMARY KEY (id),
    CONSTRAINT UN_DomainSP_Group_1 UNIQUE (superGroupId, name),
    CONSTRAINT FK_DomainSP_Group_1 FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group (id)
);

CREATE TABLE IF NOT EXISTS DomainSP_User
(
    id            INT                   NOT NULL,
    firstName     VARCHAR(100)          NULL,
    lastName      VARCHAR(100)          NOT NULL,
    phone         VARCHAR(20)           NULL,
    homePhone     VARCHAR(20)           NULL,
    cellPhone     VARCHAR(20)           NULL,
    fax           VARCHAR(20)           NULL,
    address       VARCHAR(500)          NULL,
    title         VARCHAR(100)          NULL,
    company       VARCHAR(100)          NULL,
    position      VARCHAR(100)          NULL,
    boss          VARCHAR(100)          NULL,
    login         VARCHAR(50)           NOT NULL,
    password      VARCHAR(123)          NULL,
    passwordValid CHAR(1) DEFAULT ('Y') NOT NULL,
    loginMail     VARCHAR(100)          NULL,
    email         VARCHAR(100)          NULL,
    CONSTRAINT PK_DomainSP_User PRIMARY KEY (id),
    CONSTRAINT UN_DomainSP_User_1 UNIQUE (login)
);

CREATE TABLE IF NOT EXISTS DomainSP_Group_User_Rel
(
    groupId INT NOT NULL,
    userId  INT NOT NULL,
    CONSTRAINT PK_DomainSP_Group_User_Rel PRIMARY KEY (groupId, userId),
    CONSTRAINT FK_DomainSP_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES DomainSP_Group (id),
    CONSTRAINT FK_DomainSP_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES DomainSP_User (id)
);

CREATE TABLE IF NOT EXISTS ST_Domain
(
    id                   INT                        NOT NULL,
    name                 VARCHAR(100)               NOT NULL,
    description          VARCHAR(400)               NULL,
    propFileName         VARCHAR(100)               NOT NULL,
    className            VARCHAR(100)               NOT NULL,
    authenticationServer VARCHAR(100)               NOT NULL,
    theTimeStamp         VARCHAR(100) DEFAULT ('0') NOT NULL,
    silverpeasServerURL  VARCHAR(400)               NULL,
    CONSTRAINT PK_ST_Domain PRIMARY KEY (id)
);

CREATE TABLE ST_Space
(
    id                   int             NOT NULL,
    domainFatherId       int,
    name                 varchar(100)    NOT NULL,
    description          varchar(400),
    createdBy            int,
    firstPageType        int             NOT NULL,
    firstPageExtraParam  varchar(400),
    orderNum             int DEFAULT (0) NOT NULL,
    createTime           varchar(20),
    updateTime           varchar(20),
    removeTime           varchar(20),
    spaceStatus          char(1),
    updatedBy            int,
    removedBy            int,
    lang                 char(2),
    isInheritanceBlocked int default (0) NOT NULL,
    look                 varchar(50),
    displaySpaceFirst    smallint,
    isPersonal           smallint,
    CONSTRAINT PK_Space PRIMARY KEY (id),
    CONSTRAINT UN_Space_1 UNIQUE(domainFatherId, name)
);

CREATE TABLE ST_SpaceI18N
(
    id          int          NOT NULL,
    spaceId     int          NOT NULL,
    lang        char(2)      NOT NULL,
    name        varchar(100) NOT NULL,
    description varchar(400)
);

CREATE TABLE ST_ComponentInstance
(
    id                   int             NOT NULL,
    spaceId              int             NOT NULL,
    name                 varchar(100)    NOT NULL,
    componentName        varchar(100)    NOT NULL,
    description          varchar(400),
    createdBy            int,
    orderNum             int DEFAULT (0) NOT NULL,
    createTime           varchar(20),
    updateTime           varchar(20),
    removeTime           varchar(20),
    componentStatus      char(1),
    updatedBy            int,
    removedBy            int,
    isPublic             int DEFAULT (0) NOT NULL,
    isHidden             int DEFAULT (0) NOT NULL,
    lang                 char(2),
    isInheritanceBlocked int default (0) NOT NULL
);

CREATE TABLE ST_ComponentInstanceI18N
(
    id          int          NOT NULL,
    componentId int          NOT NULL,
    lang        char(2)      NOT NULL,
    name        varchar(100) NOT NULL,
    description varchar(400)
);

CREATE TABLE IF NOT EXISTS ST_GroupUserRole
(
    id       INT          NOT NULL,
    groupId  INT          NOT NULL,
    roleName VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS ST_GroupUserRole_User_Rel
(
    groupUserRoleId INT NOT NULL,
    userId          INT NOT NULL
);

CREATE TABLE IF NOT EXISTS ST_GroupUserRole_Group_Rel
(
    groupUserRoleId INT NOT NULL,
    groupId         INT NOT NULL
);

CREATE TABLE ST_AccessLevel
(
    id   CHAR(1)      NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
    CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE ST_User
(
    id                            INT                 NOT NULL,
    domainId                      INT                 NOT NULL,
    specificId                    VARCHAR(500)        NOT NULL,
    firstName                     VARCHAR(100),
    lastName                      VARCHAR(100)        NOT NULL,
    email                         VARCHAR(100),
    login                         VARCHAR(50)         NOT NULL,
    loginMail                     VARCHAR(100),
    accessLevel                   CHAR(1) DEFAULT 'U' NOT NULL,
    loginquestion                 VARCHAR(200),
    loginanswer                   VARCHAR(200),
    creationDate                  TIMESTAMP,
    saveDate                      TIMESTAMP,
    version                       INT     DEFAULT 0   NOT NULL,
    tosAcceptanceDate             TIMESTAMP,
    lastLoginDate                 TIMESTAMP,
    nbSuccessfulLoginAttempts     INT     DEFAULT 0   NOT NULL,
    lastLoginCredentialUpdateDate TIMESTAMP,
    expirationDate                TIMESTAMP,
    state                         VARCHAR(30)         NOT NULL,
    stateSaveDate                 TIMESTAMP           NOT NULL,
    notifManualReceiverLimit      INT,
    CONSTRAINT PK_User PRIMARY KEY (id),
    CONSTRAINT UN_User_1 UNIQUE (specificId, domainId),
    CONSTRAINT UN_User_2 UNIQUE (login, domainId),
    CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id)
);

/*
 Groups
 */

CREATE TABLE ST_Group
(
    id           int          NOT NULL,
    domainId     int          NOT NULL,
    specificId   varchar(500) NOT NULL,
    superGroupId int,
    name         varchar(100) NOT NULL,
    description  varchar(400),
    synchroRule  varchar(100),
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
    CONSTRAINT UN_UserRole_1 UNIQUE (instanceId, roleName, isInherited, objectId)
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

CREATE TABLE SB_ContentManager_Instance
(
    instanceId    int          NOT NULL,
    componentId   varchar(100) NOT NULL,
    containerType varchar(100) NOT NULL,
    contentType   varchar(100) NOT NULL,
    CONSTRAINT PK_ContentManager_Instance PRIMARY KEY (instanceId)
);

CREATE TABLE SB_ContentManager_Content
(
    silverContentId   int          NOT NULL,
    internalContentId varchar(100) NOT NULL,
    contentInstanceId int          NOT NULL,
    authorId          int          NOT NULL,
    creationDate      date         NOT NULL,
    beginDate         varchar(10)  NULL,
    endDate           varchar(10)  NULL,
    isVisible         int          NULL,
    CONSTRAINT PK_ContentManager_Content PRIMARY KEY (silverContentId)
);

create table sb_sn_relationship
(
    id                 int       not null,
    user1id            int       not null,
    user2id            int       not null,
    typerelationshipid int,
    acceptancedate     timestamp not null,
    inviterid          int       not null,
    CONSTRAINT pk_sb_sn_relationship PRIMARY KEY (id)
);