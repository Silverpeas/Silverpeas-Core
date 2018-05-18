CREATE TABLE IF NOT EXISTS SB_Delegations (
  id             VARCHAR(40)   NOT NULL,
  delegatorId    VARCHAR(40)   NOT NULL,
  delegateId     VARCHAR(40)   NOT NULL,
  instanceId     VARCHAR(30)   NOT NULL,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  CONSTRAINT PK_DELEGATION PRIMARY KEY (id)
)