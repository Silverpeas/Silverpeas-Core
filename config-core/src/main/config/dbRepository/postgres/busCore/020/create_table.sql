CREATE TABLE ST_AccessLevel
(
    id   char(1)       NOT NULL,
    name varchar(100)  NOT NULL
);

CREATE TABLE ST_User 
(
    id          int           NOT NULL,
    domainId    int           NOT NULL,
    specificId  varchar(500)  NOT NULL,
    firstName   varchar(100),
    lastName    varchar(100)  NOT NULL,
    email       varchar(100),
    login       varchar(50)   NOT NULL,
    loginMail   varchar(100),
    accessLevel char(1)       DEFAULT 'U' NOT NULL,
	loginquestion varchar(200),
  	loginanswer varchar(200)
);

CREATE TABLE ST_Group
(
    id              int           NOT NULL,
    domainId        int           NOT NULL,
    specificId      varchar(500)  NOT NULL,
    superGroupId    int,
    name            varchar(100)  NOT NULL,
    description     varchar(400),
    synchroRule	    varchar(100)
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
    look			varchar(50)
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

CREATE TABLE ST_UserSetType
(
    id   char(1)       NOT NULL,
    name varchar(100)  NOT NULL
);

CREATE TABLE ST_UserSet
(
    userSetType char(1) NOT NULL,
    userSetId   int     NOT NULL
);

CREATE TABLE ST_UserSet_UserSet_Rel
(
    superSetType char(1) NOT NULL,
    superSetId   int     NOT NULL,
    subSetType   char(1) NOT NULL,
    subSetId     int     NOT NULL,
    linksCount   int     NOT NULL
);

CREATE TABLE ST_UserSet_User_Rel
(
    userSetType char(1) NOT NULL,
    userSetId   int     NOT NULL,
    userId      int     NOT NULL,
    linksCount  int     NOT NULL
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
	password	varchar (32) NULL ,
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
	login		varchar(50)	NOT NULL ,
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
	modelId			varchar(50)     NOT NULL
) 
;