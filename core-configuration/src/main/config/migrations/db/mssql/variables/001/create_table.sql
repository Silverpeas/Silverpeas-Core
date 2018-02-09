CREATE TABLE SB_Variables_Variable (
  id             VARCHAR(40)    NOT NULL,
  label          VARCHAR(255)   NOT NULL,
  description    VARCHAR(2000),
  createDate     DATETIME       NOT NULL,
  createdBy      VARCHAR(40)    NOT NULL,
  lastUpdateDate DATETIME       NOT NULL,
  lastUpdatedBy  VARCHAR(40)    NOT NULL,
  version        BIGINT         NOT NULL,
  CONSTRAINT PK_Variables_Variable PRIMARY KEY (id)
);

CREATE TABLE SB_Variables_Period (
  id             VARCHAR(40)    NOT NULL,
  variableId     VARCHAR(40)    NOT NULL,
  value          VARCHAR(2000)  NOT NULL,
  startDate      DATETIME       NOT NULL,
  endDate        DATETIME       NOT NULL,
  createDate     DATETIME       NOT NULL,
  createdBy      VARCHAR(40)    NOT NULL,
  lastUpdateDate DATETIME       NOT NULL,
  lastUpdatedBy  VARCHAR(40)    NOT NULL,
  version        BIGINT         NOT NULL,
  CONSTRAINT PK_Variables_Period PRIMARY KEY (id),
  CONSTRAINT FK_Variables_Period FOREIGN KEY (valueId) REFERENCES SB_Variables_Variable(id)
);
