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

CREATE TABLE SB_Variables_Period (
  id             VARCHAR(40)    NOT NULL,
  variableId     VARCHAR(40)    NOT NULL,
  value          VARCHAR(255)   NOT NULL,
  startDate      TIMESTAMP      NOT NULL,
  endDate        TIMESTAMP      NOT NULL,
  createDate     TIMESTAMP      NOT NULL,
  createdBy      VARCHAR(40)    NOT NULL,
  lastUpdateDate TIMESTAMP      NOT NULL,
  lastUpdatedBy  VARCHAR(40)    NOT NULL,
  version        INT8           NOT NULL,
  CONSTRAINT PK_Variables_Period PRIMARY KEY (id),
  CONSTRAINT FK_Variables_Period FOREIGN KEY (valueId) REFERENCES SB_Variables_Variable(id)
);
