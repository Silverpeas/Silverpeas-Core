CREATE TABLE SB_Workflow_Replacements (
  id             VARCHAR(40)  NOT NULL,
  incumbentId    VARCHAR(40)  NOT NULL,
  substituteId   VARCHAR(40)  NOT NULL,
  workflowId     VARCHAR(40)  NOT NULL,
  startDate      DATETIME     NOT NULL,
  endDate        DATETIME     NOT NULL,
  inDays         BIT          NOT NULL,
  createDate     DATETIME     NOT NULL,
  createdBy      VARCHAR(40)  NOT NULL,
  lastUpdateDate DATETIME     NOT NULL,
  lastUpdatedBy  VARCHAR(40)  NOT NULL,
  version        BIGINT       NOT NULL,
  CONSTRAINT PK_REPLACEMENT PRIMARY KEY (id)
);

CREATE INDEX IDX_INCUMBENT ON SB_Workflow_Replacements(incumbentId, workflowId);
CREATE INDEX IDX_SUBSTITUTE ON SB_Workflow_Replacements(substituteId, workflowId);