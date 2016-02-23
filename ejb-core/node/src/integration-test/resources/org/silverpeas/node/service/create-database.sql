CREATE TABLE SB_Node_Node
(
  nodeId           INT              NOT NULL,
  nodeName         VARCHAR(1000)    NOT NULL,
  nodeDescription  VARCHAR(2000)    NULL,
  nodeCreationDate VARCHAR(10)      NOT NULL,
  nodeCreatorId    VARCHAR(100)     NOT NULL,
  nodePath         VARCHAR(1000)    NOT NULL,
  nodeLevelNumber  INT              NOT NULL,
  nodeFatherId     INT              NOT NULL,
  modelId          VARCHAR(1000)    NULL,
  nodeStatus       VARCHAR(1000)    NULL,
  instanceId       VARCHAR(50)      NOT NULL,
  type             VARCHAR(50)      NULL,
  orderNumber      INT DEFAULT (0)  NULL,
  lang             CHAR(2),
  rightsDependsOn  INT DEFAULT (-1) NOT NULL
);

CREATE TABLE SB_Node_NodeI18N
(
  id              INT           NOT NULL,
  nodeId          INT           NOT NULL,
  lang            CHAR(2)       NOT NULL,
  nodeName        VARCHAR(1000) NOT NULL,
  nodeDescription VARCHAR(2000)
);

ALTER TABLE SB_Node_Node ADD
CONSTRAINT PK_Node_Node
PRIMARY KEY
  (
    nodeId,
    instanceId
  );
ALTER TABLE SB_Node_NodeI18N ADD
CONSTRAINT PK_Node_NodeI18N
PRIMARY KEY
  (
    id
  );
