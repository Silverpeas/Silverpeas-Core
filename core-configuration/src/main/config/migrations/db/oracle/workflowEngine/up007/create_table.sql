CREATE TABLE SB_Workflow_Replacements (
  id             VARCHAR(40)   NOT NULL,
  incumbentId    VARCHAR(40)   NOT NULL,
  substituteId   VARCHAR(40)   NOT NULL,
  workflowId     VARCHAR(40)   NOT NULL,
  startDate      DATE          NOT NULL,
  endDate        DATE          NOT NULL,
  inDays         NUMBER(1,0)   NOT NULL,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        NUMBER(19, 0) NOT NULL,
  CONSTRAINT PK_REPLACEMENT PRIMARY KEY (id)
);

CREATE INDEX IDX_INCUMBENT ON SB_Workflow_Replacements(incumbentId, workflowId);
CREATE INDEX IDX_SUBSTITUTE ON SB_Workflow_Replacements(substituteId, workflowId);