CREATE TABLE IF NOT EXISTS ST_AccessLevel
(
  id   CHAR(1)      NOT NULL,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
  CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS ST_User (
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
  CONSTRAINT PK_User PRIMARY KEY (id),
  CONSTRAINT UN_User_1 UNIQUE (specificId, domainId),
  CONSTRAINT UN_User_2 UNIQUE (login, domainId),
  CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id)
);

CREATE TABLE IF NOT EXISTS ST_Space (
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

CREATE TABLE IF NOT EXISTS ST_UserFavoriteSpaces
(
  id      INT NOT NULL,
  userid  INT NOT NULL,
  spaceid INT NOT NULL,
  CONSTRAINT PK_UserFavoriteSpaces PRIMARY KEY (id),
  CONSTRAINT FK_UserFavoriteSpaces_1 FOREIGN KEY (userid) REFERENCES ST_User (id),
  CONSTRAINT FK_UserFavoriteSpaces_2 FOREIGN KEY (spaceid) REFERENCES ST_Space (id)
);