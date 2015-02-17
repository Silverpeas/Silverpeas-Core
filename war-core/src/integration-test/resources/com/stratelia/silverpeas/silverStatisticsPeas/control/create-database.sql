
CREATE TABLE ST_User
(
  id                            INT                  NOT NULL,
  domainId                      INT                  NOT NULL,
  specificId                    VARCHAR(500)         NOT NULL,
  firstName                     VARCHAR(100),
  lastName                      VARCHAR(100)         NOT NULL,
  email                         VARCHAR(100),
  login                         VARCHAR(50)          NOT NULL,
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
    synchroRule	    varchar(100)
);

CREATE TABLE ST_Group_User_Rel
(
    groupId int NOT NULL,
    userId  int NOT NULL
);

CREATE TABLE SB_Stat_Connection
(
    dateStat        varchar(10)		not null,
    userId          integer		not null,
    countConnection decimal(19)	        not null,
    duration        decimal(19)	        not null
);


CREATE TABLE SB_Stat_Access
(
    dateStat        	varchar(10)	not null,
    userId          	integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countAccess		decimal(19)     not null
);

CREATE TABLE SB_Stat_SizeDir
(
    dateStat        varchar(10)	        not null,
    fileDir         varchar(256)        not null,
    sizeDir         decimal(19)		not null
);

CREATE TABLE SB_Stat_Volume
(
    dateStat        varchar(10)		not null,
    userId          integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countVolume		decimal(19)     not null
);

CREATE TABLE SB_Stat_ConnectionCumul
(
    dateStat        varchar(10)  	not null,
    userId          integer		not null,
    countConnection decimal(19)	        not null,
    duration        decimal(19)	        not null
);


CREATE TABLE SB_Stat_AccessCumul
(
    dateStat        	varchar(10)	not null,
    userId          	integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countAccess		decimal(19)     not null
);


CREATE TABLE SB_Stat_SizeDirCumul
(
    dateStat        varchar(10)	        not null,
    fileDir         varchar(256)        not null,
    sizeDir         decimal(19)		not null
);

CREATE TABLE SB_Stat_VolumeCumul
(
    dateStat        varchar(10)		not null,
    userId          integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countVolume		decimal(19)     not null
);
