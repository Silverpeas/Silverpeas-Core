CREATE TABLE SB_PDC_Subscription
(
  id   int          NOT NULL,
  name varchar(255) NOT NULL,
  CONSTRAINT PK_PDC_Subscription PRIMARY KEY (id)
);

CREATE TABLE SB_PDC_Subscription_Axis
(
  id                int NOT NULL,
  pdcSubscriptionId int NOT NULL,
  axisId            int NOT NULL,
  val               varchar(100),
  CONSTRAINT PK_PDC_Subscription_Axis PRIMARY KEY (id)
);

CREATE TABLE subscribe
(
  subscriberId       VARCHAR(100) NOT NULL,
  subscriberType     VARCHAR(50)  NOT NULL,
  subscriptionMethod VARCHAR(50)  NOT NULL,
  resourceId         VARCHAR(100) NOT NULL,
  resourceType       VARCHAR(50)  NOT NULL,
  space              VARCHAR(50)  NOT NULL,
  instanceId         VARCHAR(50)  NOT NULL,
  creatorId          VARCHAR(100) NOT NULL,
  creationDate       TIMESTAMP    NOT NULL
);

CREATE TABLE SB_ContentManager_Instance
(
  instanceId    int          NOT NULL,
  componentId   varchar(100) NOT NULL,
  containerType varchar(100) NOT NULL,
  contentType   varchar(100) NOT NULL
);

CREATE TABLE SB_ContentManager_Content
(
  silverContentId   int          NOT NULL,
  internalContentId varchar(100) NOT NULL,
  contentInstanceId int          NOT NULL,
  authorId          int          NOT NULL,
  creationDate      date         NOT NULL,
  beginDate         varchar(10)  NULL,
  endDate           varchar(10)  NULL,
  isVisible         int          NULL
);
