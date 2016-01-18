CREATE TABLE sb_comment_comment
(
  commentId               INT           NOT NULL,
  commentOwnerId          INT           NOT NULL,
  commentCreationDate     CHAR(10)      NOT NULL,
  commentModificationDate CHAR(10),
  commentComment          VARCHAR(2000) NOT NULL,
  instanceId              VARCHAR(50)   NOT NULL,
  resourceType            VARCHAR(50)   NOT NULL,
  resourceId              VARCHAR(50)   NOT NULL
);

ALTER TABLE sb_comment_comment  ADD
CONSTRAINT PK_Comment_Comment PRIMARY KEY
  (
    commentid
  );

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