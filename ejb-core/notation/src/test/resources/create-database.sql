CREATE TABLE SB_Notation_Notation
(
  id           INT         NOT NULL,
  instanceId   VARCHAR(50) NOT NULL,
  externalId   VARCHAR(50) NOT NULL,
  externalType VARCHAR(50) NOT NULL,
  author       VARCHAR(50) NOT NULL,
  note         INT         NOT NULL
);

CREATE TABLE uniqueId (
  maxId     int           NOT NULL ,
  tableName varchar(100)  NOT NULL
);