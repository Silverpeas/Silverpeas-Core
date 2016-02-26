CREATE TABLE SB_Pdc_Axis
(
  id           INT           NOT NULL,
  RootId       INT           NOT NULL,
  Name         VARCHAR(255)  NOT NULL,
  AxisType     CHAR(1)       NOT NULL,
  AxisOrder    INT           NOT NULL,
  creationDate VARCHAR(10)   NULL,
  creatorId    VARCHAR(255)  NULL,
  description  VARCHAR(1000) NULL,
  lang         CHAR(2)       NULL,
  CONSTRAINT PK_Pdc_Axis PRIMARY KEY (id)
);

CREATE TABLE SB_Pdc_Utilization
(
  id         INT          NOT NULL,
  instanceId VARCHAR(100) NOT NULL,
  axisId     INT          NOT NULL,
  baseValue  INT          NOT NULL,
  mandatory  INT          NOT NULL,
  variant    INT          NOT NULL,
  CONSTRAINT PK_Pdc_Utilization PRIMARY KEY (id)
);

CREATE TABLE SB_Pdc_AxisI18N
(
  id          INT           NOT NULL,
  AxisId      INT           NOT NULL,
  lang        CHAR(2)       NOT NULL,
  Name        VARCHAR(255)  NOT NULL,
  description VARCHAR(1000) NULL,
  CONSTRAINT PK_Pdc_AxisI18N PRIMARY KEY (id)
);

CREATE TABLE SB_Pdc_User_Rights
(
  axisId  INT NOT NULL,
  valueId INT NOT NULL,
  userId  INT NOT NULL,
  CONSTRAINT FK_Pdc_User_Rights_1 FOREIGN KEY (axisId) REFERENCES SB_Pdc_Axis (id),
  CONSTRAINT FK_Pdc_User_Rights_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE SB_Pdc_Group_Rights
(
  axisId  INT NOT NULL,
  valueId INT NOT NULL,
  groupId INT NOT NULL,
  CONSTRAINT FK_Pdc_Group_Rights_1 FOREIGN KEY (axisId) REFERENCES SB_Pdc_Axis (id),
  CONSTRAINT FK_Pdc_Group_Rights_2 FOREIGN KEY (groupId) REFERENCES ST_Group (id)
);

CREATE TABLE PdcAxisValue (
  valueId INT8 NOT NULL,
  axisId  INT8 NOT NULL,
  PRIMARY KEY (valueId, axisId)
);

CREATE TABLE PdcClassification (
  id         INT8         NOT NULL,
  contentId  VARCHAR(255),
  instanceId VARCHAR(255) NOT NULL,
  modifiable BOOL         NOT NULL,
  nodeId     VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE PdcPosition (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE PdcClassification_PdcPosition (
  PdcClassification_id INT8 NOT NULL,
  positions_id         INT8 NOT NULL,
  PRIMARY KEY (PdcClassification_id, positions_id),
  UNIQUE (positions_id),
  CONSTRAINT FK_PdcClassification_PdcPosition_PositionId FOREIGN KEY (positions_id) REFERENCES PdcPosition,
  CONSTRAINT FK_PdcClassification_PdcPosition_PositionId_PdcClassificationId FOREIGN KEY (PdcClassification_id) REFERENCES PdcClassification
);

CREATE TABLE PdcPosition_PdcAxisValue (
  PdcPosition_id     INT8   NOT NULL,
  axisValues_valueId INT8   NOT NULL,
  axisValues_axisId  BIGINT NOT NULL,
  PRIMARY KEY (PdcPosition_id, axisValues_valueId, axisValues_axisId),
  CONSTRAINT FK_PdcPosition_PdcAxisValue_PdcAxisValueId FOREIGN KEY (axisValues_valueId, axisValues_axisId) REFERENCES PdcAxisValue,
  CONSTRAINT FK_PdcPosition_PdcAxisValue_PdcPositionId FOREIGN KEY (PdcPosition_id) REFERENCES PdcPosition
);
