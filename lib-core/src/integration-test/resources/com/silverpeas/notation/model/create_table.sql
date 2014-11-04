CREATE TABLE IF NOT EXISTS SB_Notation_Notation
(
  id           INT         NOT NULL,
  instanceId   VARCHAR(50) NOT NULL,
  externalId   VARCHAR(50) NOT NULL,
  externalType VARCHAR(50) NOT NULL,
  author       VARCHAR(50) NOT NULL,
  note         INT         NOT NULL,
  CONSTRAINT PK_SB_Notation_Notation PRIMARY KEY (id),
  CONSTRAINT UN_SB_Notation_Notation UNIQUE (instanceId, externalId, externalType, author)
);
