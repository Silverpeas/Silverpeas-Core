CREATE TABLE IF NOT EXISTS UniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);

-- Domain

CREATE TABLE ST_Domain (
  id                   INT                        NOT NULL,
  name                 VARCHAR(100)               NOT NULL,
  description          VARCHAR(400)               NULL,
  propFileName         VARCHAR(100)               NOT NULL,
  className            VARCHAR(100)               NOT NULL,
  authenticationServer VARCHAR(100)               NOT NULL,
  theTimeStamp         VARCHAR(100) DEFAULT ('0') NOT NULL,
  silverpeasServerURL  VARCHAR(400)               NULL
);
ALTER TABLE ST_Domain  ADD CONSTRAINT PK_ST_Domain PRIMARY KEY (id);

INSERT INTO ST_Domain (id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
VALUES (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-', '0', '');
INSERT INTO ST_Domain (id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
VALUES (0, 'domainSilverpeas', 'default domain for Silverpeas',
        'org.silverpeas.domains.domainSP',
        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', '0',
        '${URLSERVER}');

CREATE TABLE DomainSP_User (
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
  password      VARCHAR(32)           NULL,
  passwordValid CHAR(1) DEFAULT ('Y') NOT NULL,
  loginMail     VARCHAR(100)          NULL,
  email         VARCHAR(100)          NULL
);
ALTER TABLE DomainSP_User ADD CONSTRAINT PK_DomainSP_User PRIMARY KEY (id);
ALTER TABLE DomainSP_User ADD CONSTRAINT UN_DomainSP_User_1 UNIQUE (login);

INSERT INTO DomainSP_User (id, lastName, login, password)
VALUES (0, 'Administrateur', '${ADMINLOGIN}', '${ADMINPASSWD}');

CREATE TABLE DomainSP_Group (
  id           INT          NOT NULL,
  supergroupid INT,
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(400),
  FOREIGN KEY (supergroupid) REFERENCES DomainSP_Group (id)
);
ALTER TABLE DomainSP_Group ADD CONSTRAINT PK_DomainSP_Group PRIMARY KEY (id);
ALTER TABLE DomainSP_Group ADD CONSTRAINT UN_DomainSP_Group_1 UNIQUE (superGroupId, name);
ALTER TABLE DomainSP_Group ADD CONSTRAINT FK_DomainSP_Group_1 FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group (id);

CREATE TABLE DomainSP_Group_User_Rel (
  groupId INT NOT NULL,
  userId  INT NOT NULL
);
ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES DomainSP_Group (id);
ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES DomainSP_User (id);

-- Users for Silverpeas application

CREATE TABLE ST_AccessLevel (
  id   CHARACTER(1)           NOT NULL,
  name CHARACTER VARYING(100) NOT NULL
);
ALTER TABLE ST_AccessLevel  ADD CONSTRAINT PK_AccessLevel PRIMARY KEY (id);
ALTER TABLE ST_AccessLevel ADD CONSTRAINT UN_AccessLevel_1 UNIQUE (name);

INSERT INTO ST_AccessLevel (id, name) VALUES ('U', 'User');
INSERT INTO ST_AccessLevel (id, name) VALUES ('A', 'Administrator');
INSERT INTO ST_AccessLevel (id, name) VALUES ('G', 'Guest');
INSERT INTO ST_AccessLevel (id, name) VALUES ('R', 'Removed');
INSERT INTO ST_AccessLevel (id, name) VALUES ('K', 'KMManager');
INSERT INTO ST_AccessLevel (id, name) VALUES ('D', 'DomainManager');

CREATE TABLE ST_User (
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
ALTER TABLE ST_User ADD CONSTRAINT PK_User PRIMARY KEY (id);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_1 UNIQUE (specificId, domainId);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_2 UNIQUE (login, domainId);
ALTER TABLE ST_User ADD CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id);

INSERT INTO ST_User (id, specificId, domainId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (0, '0', 0, 'Administrateur', '${ADMINLOGIN}', 'A', 'VALID', CURRENT_TIMESTAMP);

CREATE TABLE ST_Group (
  id           INT          NOT NULL,
  domainId     INT          NOT NULL,
  specificId   VARCHAR(500) NOT NULL,
  superGroupId INT,
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(400),
  synchroRule  VARCHAR(100)
);
ALTER TABLE ST_Group ADD CONSTRAINT PK_Group PRIMARY KEY (id);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_1 UNIQUE (specificId, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_2 UNIQUE (superGroupId, name, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group (id);

CREATE TABLE ST_Group_User_Rel (
  groupId INT NOT NULL,
  userId  INT NOT NULL
);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group (id);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id);

-- Personalization
CREATE TABLE Personalization (
  id                  VARCHAR(100) NOT NULL,
  languages           VARCHAR(100) NULL,
  look                VARCHAR(50)  NULL,
  personalWSpace      VARCHAR(50)  NULL,
  thesaurusStatus     INT          NOT NULL,
  dragAndDropStatus   INT DEFAULT 1,
  webdavEditingStatus INT DEFAULT 0,
  menuDisplay         VARCHAR(50) DEFAULT 'DEFAULT'
);
ALTER TABLE Personalization  ADD CONSTRAINT PK_Personalization PRIMARY KEY (id);