CREATE TABLE IF NOT EXISTS st_quota (
  id           INT8 PRIMARY KEY NOT NULL,
  quotaType    VARCHAR(50)      NOT NULL,
  resourceId   VARCHAR(50)      NOT NULL,
  minCount     INT8             NOT NULL,
  maxCount     INT8             NOT NULL,
  currentCount INT8             NOT NULL,
  saveDate     TIMESTAMP        NOT NULL
);
