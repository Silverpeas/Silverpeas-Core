-- Copy temporary subscribe table with current data
SELECT
  *
INTO subscribe_tmp
FROM subscribe;

-- Drop current table
DROP TABLE subscribe;

-- Create subscribe table with its new structure
CREATE TABLE subscribe (
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
    '1970-01-01 00:00:00.0'
  FROM subscribe_tmp;

-- Drop temporary subscribe table
DROP TABLE subscribe_tmp;