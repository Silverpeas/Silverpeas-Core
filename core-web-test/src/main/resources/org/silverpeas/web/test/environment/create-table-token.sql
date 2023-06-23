CREATE TABLE st_token (
  id         INT8        NOT NULL,
  tokenType  VARCHAR(50) NOT NULL,
  resourceId VARCHAR(50) NOT NULL,
  token      VARCHAR(50) NOT NULL,
  saveCount  INT         NOT NULL,
  saveDate   TIMESTAMP   NOT NULL
);