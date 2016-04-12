CREATE TABLE SB_MyLinks_Link (
  linkId      INT          NOT NULL,
  name        VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  url         VARCHAR(255) NOT NULL,
  visible     INT          NOT NULL,
  popup       INT          NOT NULL,
  userId      VARCHAR(50)  NOT NULL,
  instanceId  VARCHAR(50)  NULL,
  objectId    VARCHAR(50)  NULL,
  position    INT          NULL
);