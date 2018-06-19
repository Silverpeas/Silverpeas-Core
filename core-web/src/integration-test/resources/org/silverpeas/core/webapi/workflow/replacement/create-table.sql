CREATE TABLE IF NOT EXISTS SB_Workflow_Replacements (
  id             VARCHAR(40)   NOT NULL,
  incumbentId    VARCHAR(40)   NOT NULL,
  substituteId   VARCHAR(40)   NOT NULL,
  workflowId     VARCHAR(40)   NOT NULL,
  startDate      TIMESTAMP     NOT NULL,
  endDate        TIMESTAMP     NOT NULL,
  inDays         BOOLEAN       NOT NULL,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  CONSTRAINT PK_REPLACEMENT PRIMARY KEY (id)
);