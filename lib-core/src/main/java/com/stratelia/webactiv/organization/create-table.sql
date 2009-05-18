CREATE TABLE ST_AccessLevel
(
    id   char(1)       NOT NULL PRIMARY KEY,
    name varchar(100)  NOT NULL UNIQUE,
);
insert into ST_AccessLevel values ('U', 'User');
insert into ST_AccessLevel values ('A', 'Administrator');

CREATE TABLE ST_Domain 
(
    id            int           NOT NULL PRIMARY KEY,
    name          varchar(100)  NOT NULL,
    description   varchar(400),
    propFileName  varchar(100)  NOT NULL,
    className     varchar(100)  NOT NULL
)

CREATE TABLE ST_User 
(
    id        int           NOT NULL PRIMARY KEY,
    specificId    varchar(500)  NOT NULL,
    domainId  int NOT NULL,
    login     varchar(20)   NOT NULL UNIQUE,
    firstName varchar(100),
    lastName  varchar(100)  NOT NULL,
    loginMail varchar(100),
    accessLevel char(1)     NOT NULL DEFAULT 'U',

    UNIQUE(domainId, specificId),
    FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel(id),
    FOREIGN KEY (domainId)    REFERENCES ST_Domain(id)
);

CREATE TABLE ST_Group
(
    id            int           NOT NULL PRIMARY KEY,
    specificId        varchar(500)  NOT NULL,
    domainId      int,
    superGroupId  int,
    name          varchar(100)  NOT NULL,
    description   varchar(400),

    UNIQUE(superGroupId, name),
    UNIQUE(domainId, specificId),
    FOREIGN KEY (superGroupId) REFERENCES ST_Group(id) , --ON DELETE CASCADE
    FOREIGN KEY (domainId)    REFERENCES ST_Domain(id)
);

CREATE TABLE ST_Group_User_Rel
(
    groupId  int NOT NULL,
    userId  int NOT NULL,
    
    PRIMARY KEY (groupId, userId),
    FOREIGN KEY (groupId) REFERENCES ST_Group(id) , --ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES ST_User(id) , --ON DELETE CASCADE
);

CREATE TABLE DomainSP_User 
(
    id        int           NOT NULL PRIMARY KEY,
    firstName varchar(100),
    lastName  varchar(100)  NOT NULL,
    email     varchar(100),
    phone     varchar(20),
    homePhone varchar(20),
    cellPhone varchar(20),
    fax       varchar(20),
    address   varchar(500),
    title     varchar(100),
    company   varchar(100),
    position  varchar(100),
    boss      varchar(100),
    login     varchar(20)   NOT NULL UNIQUE,
    password  varchar(32),
    loginMail varchar(100),
);

CREATE TABLE DomainSP_Group
(
    id            int           NOT NULL PRIMARY KEY,
    superGroupId  int,
    name          varchar(100)  NOT NULL,
    description   varchar(400),

    UNIQUE(superGroupId, name),
    FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group(id) , --ON DELETE CASCADE
);

CREATE TABLE DomainSP_Group_User_Rel
(
    groupId  int NOT NULL,
    userId  int NOT NULL,
    
    PRIMARY KEY (groupId, userId),
    FOREIGN KEY (groupId) REFERENCES DomainSP_Group(id) , --ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES DomainSP_User(id) , --ON DELETE CASCADE
);

CREATE TABLE ST_Space
(
    id			int           NOT NULL PRIMARY KEY,
    domainFatherId	int,
    name		varchar(100)  NOT NULL,
    description		varchar(400),
    createdBy		int,
    
    UNIQUE(domainFatherId, name) ,
    FOREIGN KEY (createdBy) REFERENCES ST_User(id) , --ON DELETE SET NULL
    FOREIGN KEY (domainFatherId) REFERENCES ST_Space(id) , --ON DELETE SET NULL
);

CREATE TABLE ST_ComponentInstance
(
    id            int           NOT NULL PRIMARY KEY,
    spaceId       int           NOT NULL,
    name          varchar(100)  NOT NULL,
    componentName varchar(100)  NOT NULL,
    description   varchar(400),
    createdBy     int,
    
    UNIQUE(spaceId, name),
    FOREIGN KEY (spaceId) REFERENCES ST_Space(id) , --ON DELETE CASCADE,
    FOREIGN KEY (createdBy) REFERENCES ST_User(id) ,--ON DELETE SET NULL
);

CREATE TABLE ST_Instance_Data
(
    id            int           NOT NULL PRIMARY KEY,
    componentId   int           NOT NULL,
    name          varchar(100)  NOT NULL,
    label					varchar(100)  NOT NULL,
    value					varchar(400),
);

CREATE TABLE ST_UserRole
(
    id            int           NOT NULL PRIMARY KEY,
    instanceId    int           NOT NULL,
    name          varchar(100)  NOT NULL,
    roleName      varchar(100)  NOT NULL,
    description   varchar(400),
    
    UNIQUE(instanceId, roleName),
    FOREIGN KEY (instanceId)
        REFERENCES ST_ComponentInstance(id) , --ON DELETE CASCADE
);

CREATE TABLE ST_UserRole_User_Rel
(
    userRoleId   int NOT NULL,
    userId       int NOT NULL,
    
	PRIMARY KEY (userRoleId, userId),
    FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id) , --ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES ST_User(id), --ON DELETE CASCADE
);

CREATE TABLE ST_UserRole_Group_Rel
(
    userRoleId   int NOT NULL,
    groupId      int NOT NULL,
    
	PRIMARY KEY (userRoleId, groupId),
    FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id) , --ON DELETE CASCADE,
    FOREIGN KEY (groupId) REFERENCES ST_Group(id), --ON DELETE CASCADE
);

CREATE TABLE ST_SpaceUserRole
(
    id            int           NOT NULL PRIMARY KEY,
    spaceId	  int           NOT NULL,
    name          varchar(100)  NOT NULL,
    roleName      varchar(100)  NOT NULL,
    description   varchar(400),
    
    UNIQUE(spaceId, roleName),
    FOREIGN KEY (spaceId) REFERENCES ST_Space(id) , --ON DELETE CASCADE
);

CREATE TABLE ST_SpaceUserRole_User_Rel
(
    spaceUserRoleId   int NOT NULL,
    userId            int NOT NULL,
    
    PRIMARY KEY (spaceUserRoleId, userId),
    FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id) , --ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES ST_User(id), --ON DELETE CASCADE
);

CREATE TABLE ST_SpaceUserRole_Group_Rel
(
    spaceUserRoleId   int NOT NULL,
    groupId           int NOT NULL,
    
    PRIMARY KEY (spaceUserRoleId, groupId),
    FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id) , --ON DELETE CASCADE,
    FOREIGN KEY (groupId) REFERENCES ST_Group(id), --ON DELETE CASCADE
);

CREATE TABLE ST_UserSetType
(
    id   char(1)       NOT NULL PRIMARY KEY,
    name varchar(100)  NOT NULL UNIQUE,
);
insert into ST_UserSetType values ('G', 'Group');
insert into ST_UserSetType values ('R', 'UserRole');
insert into ST_UserSetType values ('I', 'ComponentInstance');
insert into ST_UserSetType values ('S', 'Space');
insert into ST_UserSetType values ('M', 'SpaceProfile');

CREATE TABLE ST_UserSet
(
    userSetType char(1) NOT NULL,
    userSetId   int     NOT NULL,

    PRIMARY KEY (userSetType, userSetId),
    FOREIGN KEY (userSetType) REFERENCES ST_UserSetType(id),

    -- si ON DELETE CASCADE on pourrait ajouter :
    -- ifGroup     int,
    -- ifUserRole  int,
    -- ifInstance  int,
    -- ifSpace     int,
    -- FOREIGN KEY (ifGroup)
    --     REFERENCES ST_Group(id) ON DELETE CASCADE,
    -- FOREIGN KEY (ifUserRole)
    --     REFERENCES ST_UserRole(id) ON DELETE CASCADE,
    -- FOREIGN KEY (ifInstance)
    --     REFERENCES ST_ComponentInstance(id) ON DELETE CASCADE,
    -- FOREIGN KEY (ifSpace)
    --     REFERENCES ST_Space(id) ON DELETE CASCADE
);

CREATE TABLE ST_UserSet_UserSet_Rel
(
    superSetType char(1) NOT NULL,
    superSetId   int     NOT NULL,
    subSetType   char(1) NOT NULL,
    subSetId     int     NOT NULL,
    linksCount   int     NOT NULL,

    PRIMARY KEY (superSetType, subSetType, superSetId, subSetId),
    FOREIGN KEY (superSetType, superSetId)
        REFERENCES ST_UserSet(userSetType, userSetId), --ON DELETE CASCADE,
    FOREIGN KEY (subSetType, subSetId)
        REFERENCES ST_UserSet(userSetType, userSetId), --ON DELETE CASCADE
);

ALTER TABLE st_userset_userset_rel ADD CONSTRAINT no_cycle CHECK
( 
 ((subSetType = 'R' AND superSetType = 'I') OR
(subSetType = 'R' AND superSetType = 'S') OR
(subSetType = 'I' AND superSetType = 'S') OR
(subSetType = 'S' AND superSetType = 'S') OR
(subSetType = 'M' AND superSetType = 'S') OR
(subSetType = 'G'))
AND NOT (subSetType = superSetType AND subSetId = superSetId)
)

CREATE TABLE ST_UserSet_User_Rel
(
    userSetType char(1) NOT NULL,
    userSetId   int     NOT NULL,
    userId      int     NOT NULL,
    linksCount  int     NOT NULL,

    PRIMARY KEY (userSetType, userSetId, userId),
    FOREIGN KEY (userSetType, userSetId)
        REFERENCES ST_UserSet(userSetType, userSetId), --ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES ST_User(id) --ON DELETE CASCADE
);

insert into ST_Domain(id, name, description, propFileName, className)
values             (0, 'domainSilverpeas', 'default domain for Silverpeas', 'domainSP.properties', 'SilverpeasDomainDriver.class');

insert into ST_User(id, specificId, domainId, lastName, login, accessLevel)
values             (0, 0, 0, 'Administrateur', 'admin', 'A');

insert into DomainSP_User(id, lastName, login, password)
values             (0,'Administrateur', 'admin', 'admin');
