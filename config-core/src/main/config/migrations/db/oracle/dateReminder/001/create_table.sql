CREATE TABLE st_dateReminder (
  id             VARCHAR(40) PRIMARY KEY,
  resourceType   VARCHAR(50) NOT NULL,
  resourceId     VARCHAR(50) NOT NULL ,
  dateReminder   TIMESTAMP   NOT NULL ,
  message        VARCHAR(2000),
  processStatus  INT DEFAULT 0 NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate TIMESTAMP   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        NUMBER(19, 0)        NOT NULL
);
