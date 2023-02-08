CREATE TABLE SB_Variables_Variable (
  id             VARCHAR(40)    NOT NULL,
  label          VARCHAR(255)   NOT NULL,
  description    VARCHAR(2000),
  createDate     TIMESTAMP      NOT NULL,
  createdBy      VARCHAR(40)    NOT NULL,
  lastUpdateDate TIMESTAMP      NOT NULL,
  lastUpdatedBy  VARCHAR(40)    NOT NULL,
  version        INT8           NOT NULL,
  CONSTRAINT PK_Variables_Variable PRIMARY KEY (id)
);

CREATE TABLE SB_Variables_Value (
  id             VARCHAR(40)    NOT NULL,
  variableId     VARCHAR(40)    NOT NULL,
  "value"          VARCHAR(255)   NOT NULL,
  startDate      DATE           NOT NULL,
  endDate        DATE           NOT NULL,
  createDate     TIMESTAMP      NOT NULL,
  createdBy      VARCHAR(40)    NOT NULL,
  lastUpdateDate TIMESTAMP      NOT NULL,
  lastUpdatedBy  VARCHAR(40)    NOT NULL,
  version        INT8           NOT NULL,
  CONSTRAINT PK_Variables_Value PRIMARY KEY (id),
  CONSTRAINT FK_Variables_Value FOREIGN KEY (variableId) REFERENCES SB_Variables_Variable(id)
);
