CREATE TABLE st_dateReminder (
  id             VARCHAR(40) PRIMARY KEY,
  resourceType   VARCHAR(50) NOT NULL,
  resourceId     VARCHAR(50) NOT NULL ,
  dateReminder   DATETIME   NOT NULL ,
  message        VARCHAR(2000),
  processStatus  INT NOT NULL DEFAULT 0,
  createDate     DATETIME   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate DATETIME   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        BIGINT        NOT NULL
);
