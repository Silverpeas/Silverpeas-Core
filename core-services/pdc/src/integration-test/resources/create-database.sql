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
