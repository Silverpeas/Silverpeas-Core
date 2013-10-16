-- Copy temporary subscribe table with current data
CREATE TABLE subscribe_tmp
AS SELECT
     actorId,
     nodeId,
     space,
     componentName
   FROM subscribe;

-- Drop current table
DROP TABLE subscribe;

-- Create subscribe table with its new structure
CREATE TABLE subscribe (
  subscriberId       VARCHAR2(100) NOT NULL,
  subscriberType     VARCHAR2(50)  NOT NULL,
  subscriptionMethod VARCHAR2(50)  NOT NULL,
  resourceId         VARCHAR2(100) NOT NULL,
  resourceType       VARCHAR2(50)  NOT NULL,
  space              VARCHAR2(50)  NOT NULL,
  instanceId         VARCHAR2(50)  NOT NULL,
  creatorId          VARCHAR2(100) NOT NULL,
  creationDate       TIMESTAMP     NOT NULL
);

-- Copy current data into new structure
INSERT INTO subscribe
(subscriberId, subscriberType, subscriptionMethod, resourceId, resourceType, space, instanceId, creatorId, creationDate)
  SELECT
    actorId,
    'USER',
    'SELF_CREATION',
    nodeId,
    CASE WHEN (space = 'component' OR componentName like 'blog%') THEN 'COMPONENT' ELSE 'NODE' END,
    space,
    componentName,
    actorId,
    to_date('1970-01-01', 'YYYY-MM-DD')
  FROM subscribe_tmp;

-- Copy subscriptions of infoletter into new structure
INSERT INTO subscribe
(subscriberId, subscriberType, subscriptionMethod, resourceId, resourceType, space, instanceId, creatorId, creationDate)
  SELECT
    REPLACE(REPLACE(userid,'G', ''), 'U', ''),
    case when (userid like 'U%') then 'USER' else 'GROUP' end,
    case when (userid like 'U%') then 'SELF_CREATION' else 'FORCED' end,
    '0',
    'COMPONENT',
    'component',
    instanceId,
    REPLACE(REPLACE(userid,'G', ''), 'U', ''),
    to_date('1970-01-01', 'YYYY-MM-DD')
  FROM SC_IL_IntSus;

-- Drop temporary subscribe table
DROP TABLE subscribe_tmp;

-- Drop unnecessary tables of infoletter component
DROP TABLE SC_IL_IntSus;
DROP TABLE SC_IL_Pubs;